import {
  Body,
  Controller,
  Get,
  HttpCode,
  HttpStatus,
  Post,
  Query,
} from '@nestjs/common';
import { PaymentService } from './payment.service';
import { PaymentCreateDto } from './dto/PaymentCreateDto';
import { PaymentDto } from './dto/PaymentDto';
import { PaymentQueryDto } from './dto/PaymentQueryDto';
import { CountsRequestDto } from './dto/CountsRequestDto';

@Controller('payments')
export class PaymentController {
  constructor(private readonly paymentService: PaymentService) {}

  @Post()
  @HttpCode(HttpStatus.CREATED)
  async create(@Body() dto: PaymentCreateDto): Promise<{ id: string }> {
    const id = await this.paymentService.create(dto);
    return { id };
  }

  @Get()
  async list(@Query() query: PaymentQueryDto): Promise<PaymentDto[]> {
    return this.paymentService.list(query);
  }

  @Post('_counts')
  @HttpCode(HttpStatus.OK)
  async counts(@Body() dto: CountsRequestDto): Promise<Record<string, number>> {
    return this.paymentService.getCount(dto);
  }
}
