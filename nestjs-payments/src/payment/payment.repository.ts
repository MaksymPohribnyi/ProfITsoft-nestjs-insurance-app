import { Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Payment, PaymentDocument } from './payment.schema';
import { Model } from 'mongoose';
import { PaymentCreateDto } from './dto/PaymentCreateDto';

type CountByPolicyIdResult = {
  _id: string;
  count: number;
};

@Injectable()
export class PaymentRepository {
  constructor(
    @InjectModel(Payment.name) private readonly model: Model<PaymentDocument>,
  ) {}

  async create(dto: PaymentCreateDto): Promise<string> {
    const payment = await this.model.create({
      ...dto,
      paymentDate: new Date(),
      status: dto.status || 'pending',
    });
    return payment.id;
  }

  async findByPolicyId(
    policyId: string,
    skip: number,
    limit: number,
  ): Promise<Payment[]> {
    return this.model
      .find({ policyId })
      .sort({ paymentDate: -1 })
      .skip(skip)
      .limit(limit)
      .lean();
  }

  async countByPolicyIds(policyIds: string[]): Promise<Record<string, number>> {
    const result = await this.model.aggregate<CountByPolicyIdResult>([
      {
        $match: {
          policyId: { $in: policyIds },
        },
      },
      {
        $group: {
          _id: '$policyId',
          count: { $sum: 1 },
        },
      },
    ]);

    const counts: Record<string, number> = {};

    policyIds.forEach((id) => {
      counts[id] = 0;
    });

    result.forEach((item) => {
      counts[item._id] = item.count;
    });

    return counts;
  }
}
