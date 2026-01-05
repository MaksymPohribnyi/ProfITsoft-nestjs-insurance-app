import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document } from 'mongoose';

export type PaymentDocument = Payment & Document;

export enum PaymentStatus {
  PENDING = 'pending',
  COMPLETED = 'completed',
  FAILED = 'failed',
  CANCELLED = 'cancelled',
}

export enum PaymentMethod {
  CREDIT_CARD = 'credit_card',
  BANK_TRANSFER = 'bank_transfer',
  CASH = 'cash',
  ONLINE = 'online',
}

@Schema({ timestamps: true })
export class Payment {
  @Prop({ required: true })
  policyId: string;

  @Prop({ required: true, min: 0 })
  paymentAmount: number;

  @Prop({ required: true, type: Date })
  paymentDate: Date;

  @Prop({
    required: true,
    enum: Object.values(PaymentMethod),
    type: String,
  })
  paymentMethod: string;

  @Prop({
    required: true,
    enum: Object.values(PaymentStatus),
    type: String,
    default: PaymentStatus.PENDING,
  })
  status: string;

  @Prop()
  description?: string;
}

export const PaymentSchema = SchemaFactory.createForClass(Payment);
