import { PaymentRepository } from './payment.repository';
import { PolicyValidationService } from './policy-validation.service';
import { PaymentCreateDto } from './dto/PaymentCreateDto';
import { PaymentQueryDto } from './dto/PaymentQueryDto';
import { PaymentDto } from './dto/PaymentDto';
import { plainToInstance } from 'class-transformer';
import { CountsRequestDto } from './dto/CountsRequestDto';
import { Injectable } from '@nestjs/common';

@Injectable()
export class PaymentService {
  constructor(
    private readonly paymentRepository: PaymentRepository,
    private readonly policyValidatorService: PolicyValidationService,
  ) {}

  async create(dto: PaymentCreateDto): Promise<string> {
    await this.policyValidatorService.assertExists(dto.policyId);
    return this.paymentRepository.create(dto);
  }

  async list(query: PaymentQueryDto): Promise<PaymentDto[]> {
    const payments = await this.paymentRepository.findByPolicyId(
      query.policyId,
      query.from,
      query.size,
    );
    return payments.map((payment) =>
      plainToInstance(PaymentDto, payment, { excludeExtraneousValues: true }),
    );
  }

  async getCount(dto: CountsRequestDto): Promise<Record<string, number>> {
    return this.paymentRepository.countByPolicyIds(dto.policyIds);
  }
}
