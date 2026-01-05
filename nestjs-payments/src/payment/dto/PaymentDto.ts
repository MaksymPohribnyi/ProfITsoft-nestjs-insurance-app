import { Expose, Transform } from 'class-transformer';

export class PaymentDto {
  @Expose()
  @Transform((params) => params.obj._id.toString())
  _id!: string;

  @Expose()
  policyId!: string;

  @Expose()
  paymentAmount!: number;

  @Expose()
  paymentDate!: Date;

  @Expose()
  paymentMethod!: string;

  @Expose()
  status!: string;

  @Expose()
  description?: string;

  @Expose()
  createdAt?: Date;

  @Expose()
  updatedAt?: Date;
}
