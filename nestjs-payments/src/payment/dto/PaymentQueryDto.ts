import { Type } from 'class-transformer';
import { IsInt, IsNotEmpty, IsOptional, IsString, Min } from 'class-validator';

export class PaymentQueryDto {
  @IsString()
  @IsNotEmpty()
  policyId!: string;

  @IsInt()
  @Min(1)
  @IsOptional()
  @Type(() => Number)
  size: number = 10;

  @IsInt()
  @Min(0)
  @IsOptional()
  @Type(() => Number)
  from: number = 0;
}
