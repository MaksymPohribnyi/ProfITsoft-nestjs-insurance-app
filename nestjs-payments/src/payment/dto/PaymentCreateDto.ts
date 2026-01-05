import {
  IsEnum,
  IsNotEmpty,
  IsNumber,
  IsOptional,
  IsString,
  Matches,
  Min,
} from 'class-validator';
import { PaymentMethod, PaymentStatus } from '../payment.schema';

// Validates policy ID format instead of use @IsUUID() without breaking existing legacy java service
const POLICY_ID_REGEX =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export class PaymentCreateDto {
  @Matches(POLICY_ID_REGEX, {
    message: 'policyId should be a valid UUID format',
  })
  @IsNotEmpty()
  policyId: string;

  @IsNumber()
  @Min(0, { message: 'Payment amount should be greater than or equal to 0' })
  paymentAmount!: number;

  @IsEnum(PaymentMethod, {
    message: `Payment method should be one of: ${Object.values(PaymentMethod).join(', ')}`,
  })
  paymentMethod!: string;

  @IsEnum(PaymentStatus, {
    message: `Status should be one of: ${Object.values(PaymentStatus).join(', ')}`,
  })
  @IsOptional()
  status?: string;

  @IsString()
  @IsOptional()
  description?: string;
}
