import {
  BadRequestException,
  INestApplication,
  ValidationPipe,
} from '@nestjs/common';
import { PaymentService } from './payment.service';
import { Test, TestingModule } from '@nestjs/testing';
import { PaymentController } from './payment.controller';
import { PaymentCreateDto } from './dto/PaymentCreateDto';
import request from 'supertest';

describe('PaymentController', () => {
  let app: INestApplication;
  let paymentService: PaymentService;

  beforeAll(async () => {
    const moduleRef: TestingModule = await Test.createTestingModule({
      controllers: [PaymentController],
      providers: [
        {
          provide: PaymentService,
          useValue: {
            create: jest.fn(),
            list: jest.fn(),
            getCount: jest.fn(),
          },
        },
      ],
    }).compile();

    app = moduleRef.createNestApplication();
    app.useGlobalPipes(
      new ValidationPipe({ transform: true, whitelist: true }),
    );

    await app.init();

    paymentService = moduleRef.get<PaymentService>(PaymentService);
  });

  afterAll(async () => {
    await app.close();
  });

  it('should be defined', () => {
    expect(app).toBeDefined();
    expect(paymentService).toBeDefined();
  });

  describe('POST /payments', () => {
    it('should create a payment', async () => {
      const dto: PaymentCreateDto = {
        policyId: '550e8400-e29b-41d4-a716-446655440000',
        paymentAmount: 1500,
        paymentMethod: 'credit_card',
      };
      const mockId = 'payment-test-id';
      jest.spyOn(paymentService, 'create').mockResolvedValue(mockId);

      const response = await request(app.getHttpServer())
        .post('/payments')
        .send(dto);
      expect(response.status).toBe(201);
      expect(response.body).toEqual({ id: mockId });
      expect(paymentService.create).toHaveBeenCalledWith(dto);
    });
  });

  it('should validate required fields', async () => {
    const response = await request(app.getHttpServer())
      .post('/payments')
      .send({});

    expect(response.status).toBe(400);
    expect(response.body.message).toEqual(
      expect.arrayContaining([
        'policyId should not be empty',
        'policyId should be a valid UUID format',
        'paymentAmount must be a number conforming to the specified constraints',
        expect.stringContaining('Payment method should be one of'),
      ]),
    );
  });

  it('should validate payment amount is non-negative', async () => {
    const dto = {
      policyId: '550e8400-e29b-41d4-a716-446655440000',
      paymentAmount: -100,
      paymentMethod: 'credit_card',
    };

    const response = await request(app.getHttpServer())
      .post('/payments')
      .send(dto);

    expect(response.status).toBe(400);
    expect(response.body.message).toEqual(
      expect.arrayContaining([
        'Payment amount should be greater than or equal to 0',
      ]),
    );
  });

  it('should validate payment method enum', async () => {
    const dto = {
      policyId: '550e8400-e29b-41d4-a716-446655440000',
      paymentAmount: 100,
      paymentMethod: 'invalid_method',
    };

    const response = await request(app.getHttpServer())
      .post('/payments')
      .send(dto);

    expect(response.status).toBe(400);
    expect(response.body.message).toEqual(
      expect.arrayContaining([
        expect.stringContaining('Payment method should be one of'),
      ]),
    );
  });

  it('should return 400 when policy does not exist', async () => {
    const dto: PaymentCreateDto = {
      policyId: '550e8400-e29b-41d4-a716-446655440000',
      paymentAmount: 1500,
      paymentMethod: 'credit_card',
    };

    jest
      .spyOn(paymentService, 'create')
      .mockRejectedValue(
        new BadRequestException(
          `Insurance policy with id ${dto.policyId} does not exist`,
        ),
      );

    const response = await request(app.getHttpServer())
      .post('/payments')
      .send(dto);

    expect(response.status).toBe(400);
    expect(response.body.message).toBe(
      `Insurance policy with id ${dto.policyId} does not exist`,
    );
  });

  describe('GET /payments', () => {
    it('should return list of payments', async () => {
      const policyId = '550e8400-e29b-41d4-a716-446655440000';
      const mockPayments = [
        {
          _id: 'payment-1',
          policyId,
          paymentAmount: 100,
          paymentDate: new Date('2026-01-01'),
          paymentMethod: 'cash',
          status: 'completed',
        },
        {
          _id: 'payment-2',
          policyId,
          paymentAmount: 200,
          paymentDate: new Date('2026-01-02'),
          paymentMethod: 'credit_card',
          status: 'pending',
        },
      ];

      jest.spyOn(paymentService, 'list').mockResolvedValue(mockPayments);

      const response = await request(app.getHttpServer())
        .get('/payments')
        .query({ policyId: policyId, size: 10, from: 0 });

      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(2);
      const expectedResponse = JSON.parse(JSON.stringify(mockPayments));
      expect(response.body).toEqual(expectedResponse);
      expect(paymentService.list).toHaveBeenCalledWith({
        policyId: policyId,
        size: 10,
        from: 0,
      });
    });

    it('should return empty array when no payments exist', async () => {
      jest.spyOn(paymentService, 'list').mockResolvedValue([]);

      const response = await request(app.getHttpServer())
        .get('/payments')
        .query({
          policyId: '550e8400-e29b-41d4-a716-446655440000',
          size: 10,
          from: 0,
        });

      expect(response.status).toBe(200);
      expect(response.body).toHaveLength(0);
    });

    it('should validate required payments parameter', async () => {
      const response = await request(app.getHttpServer())
        .get('/payments')
        .query({ size: 10, from: 0 });

      expect(response.status).toBe(400);
      expect(response.body.message).toEqual(
        expect.arrayContaining(['policyId should not be empty']),
      );
    });

    it('should use default pagination values', async () => {
      const policyId = '550e8400-e29b-41d4-a716-446655440000';
      jest.spyOn(paymentService, 'list').mockResolvedValue([]);

      await request(app.getHttpServer())
        .get('/payments')
        .query({ policyId: policyId });

      expect(paymentService.list).toHaveBeenCalledWith({
        policyId: policyId,
        size: 10,
        from: 0,
      });
    });
  });

  describe('POST /payments/_counts', () => {
    it('should return counts for multiple policy ids', async () => {
      const dto = {
        policyIds: [
          '550e8400-e29b-41d4-a716-446655440001',
          '550e8400-e29b-41d4-a716-446655440002',
          '550e8400-e29b-41d4-a716-446655440003',
        ],
      };

      const mockCounts = {
        '550e8400-e29b-41d4-a716-446655440001': 5,
        '550e8400-e29b-41d4-a716-446655440002': 3,
        '550e8400-e29b-41d4-a716-446655440003': 0,
      };

      jest.spyOn(paymentService, 'getCount').mockResolvedValue(mockCounts);

      const response = await request(app.getHttpServer())
        .post('/payments/_counts')
        .send(dto);

      expect(response.status).toBe(200);
      expect(response.body).toEqual(mockCounts);
      expect(paymentService.getCount).toHaveBeenCalledWith(dto);
    });

    it('should validate policyIds is an array', async () => {
      const response = await request(app.getHttpServer())
        .post('/payments/_counts')
        .send({ policyIds: 'not-an-array' });

      expect(response.status).toBe(400);
      expect(response.body.message).toEqual(
        expect.arrayContaining(['policyIds must be an array']),
      );
    });

    it('should validate policyIds contains strings', async () => {
      const response = await request(app.getHttpServer())
        .post('/payments/_counts')
        .send({ policyIds: [123, 456] });

      expect(response.status).toBe(400);
      expect(response.body.message).toEqual(
        expect.arrayContaining(['each value in policyIds must be a string']),
      );
    });
  });
});
