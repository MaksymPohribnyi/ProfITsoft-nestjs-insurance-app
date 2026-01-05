import { PaymentRepository } from './payment.repository';
import { Model } from 'mongoose';
import { Payment, PaymentDocument, PaymentSchema } from './payment.schema';
import { Test, TestingModule } from '@nestjs/testing';
import { getModelToken, MongooseModule } from '@nestjs/mongoose';
import {
  clearDatabase,
  startMongoContainer,
  stopMongoContainer,
} from './test/mongo.setup';

describe('PaymentRepository', () => {
  let paymentRepository: PaymentRepository;
  let paymentModel: Model<PaymentDocument>;

  beforeAll(async () => {
    const uri = await startMongoContainer();
    const module: TestingModule = await Test.createTestingModule({
      imports: [
        MongooseModule.forRoot(uri),
        MongooseModule.forFeature([
          { name: Payment.name, schema: PaymentSchema },
        ]),
      ],
      providers: [PaymentRepository],
    }).compile();

    paymentRepository = module.get<PaymentRepository>(PaymentRepository);
    paymentModel = module.get<Model<PaymentDocument>>(
      getModelToken(Payment.name),
    );
  });

  afterAll(async () => {
    await stopMongoContainer();
  });

  beforeEach(async () => {
    await clearDatabase();
  });

  it('should be defined', () => {
    expect(paymentRepository).toBeDefined();
  });

  describe('create', () => {
    it('should create a payment', async () => {
      const dto = {
        policyId: '550e8400-e29b-41d4-a716-446655440000',
        paymentAmount: 1500.5,
        paymentMethod: 'credit_card',
        status: 'completed',
        description: 'test payment',
      };

      const id = await paymentRepository.create(dto);

      expect(id).toBeDefined();

      const payment = await paymentModel.findById(id);
      expect(payment).toBeDefined();
      expect(payment!.policyId).toBe(dto.policyId);
      expect(payment!.paymentAmount).toBe(dto.paymentAmount);
      expect(payment!.paymentMethod).toBe(dto.paymentMethod);
      expect(payment!.status).toBe(dto.status);
      expect(payment!.description).toBe(dto.description);
      expect(payment!.paymentDate).toBeInstanceOf(Date);
    });

    it('should set default status to pending if not provided', async () => {
      const dto = {
        policyId: '550e8400-e29b-41d4-a716-446655440001',
        paymentAmount: 2000,
        paymentMethod: 'bank_transfer',
      };

      const id = await paymentRepository.create(dto);

      const payment = await paymentModel.findById(id);
      expect(payment!.status).toBe('pending');
    });

    it('should validate payment amount (negative not allowed)', async () => {
      const dto = {
        policyId: '550e8400-e29b-41d4-a716-446655440002',
        paymentAmount: -100,
        paymentMethod: 'cash',
      };

      await expect(paymentRepository.create(dto)).rejects.toMatchObject({
        name: 'ValidationError',
        message: expect.stringContaining('paymentAmount'),
      });
    });
  });

  describe('findByPolicyId', () => {
    it('should return payments sorted by date descending', async () => {
      const policyId = '550e8400-e29b-41d4-a716-446655440010';

      const payment1 = await paymentModel.create({
        policyId,
        paymentAmount: 100,
        paymentDate: new Date('2026-01-01'),
        paymentMethod: 'cash',
        status: 'completed',
      });

      const payment2 = await paymentModel.create({
        policyId,
        paymentAmount: 200,
        paymentDate: new Date('2026-01-03'),
        paymentMethod: 'credit_card',
        status: 'pending',
      });

      const payment3 = await paymentModel.create({
        policyId,
        paymentAmount: 150,
        paymentDate: new Date('2026-01-02'),
        paymentMethod: 'online',
        status: 'completed',
      });

      const payments: Payment[] = await paymentRepository.findByPolicyId(
        policyId,
        0,
        10,
      );

      expect(payments).toHaveLength(3);
      expect(payments[0].policyId.toString()).toBe(
        payment2.policyId.toString(),
      );
      expect(payments[1].policyId.toString()).toBe(
        payment3.policyId.toString(),
      );
      expect(payments[2].policyId.toString()).toBe(
        payment1.policyId.toString(),
      );
    });

    it('should respect pagination parameters', async () => {
      const policyId = '550e8400-e29b-41d4-a716-446655440011';

      // Create 5 payments
      for (let i = 0; i < 5; i++) {
        await paymentModel.create({
          policyId,
          paymentAmount: 100 * (i + 1),
          paymentDate: new Date(`2026-01-0${i + 1}`),
          paymentMethod: 'cash',
          status: 'completed',
        });
      }

      const page1 = await paymentRepository.findByPolicyId(policyId, 0, 2);
      expect(page1).toHaveLength(2);

      const page2 = await paymentRepository.findByPolicyId(policyId, 2, 2);
      expect(page2).toHaveLength(2);

      const page3 = await paymentRepository.findByPolicyId(policyId, 4, 2);
      expect(page3).toHaveLength(1);
    });

    it('should return empty array for non-existent policyId', async () => {
      const policyId = '550e8400-e29b-41d4-a716-446655440099';
      const payments = await paymentRepository.findByPolicyId(policyId, 0, 10);
      expect(payments).toHaveLength(0);
    });

    it('should filter by policyId correctly', async () => {
      const policyId1 = '550e8400-e29b-41d4-a716-446655440001';
      const policyId2 = '550e8400-e29b-41d4-a716-446655440002';

      await paymentModel.create({
        policyId: policyId1,
        paymentAmount: 100,
        paymentDate: new Date('2026-01-01'),
        paymentMethod: 'cash',
        status: 'completed',
      });

      await paymentModel.create({
        policyId: policyId2,
        paymentAmount: 200,
        paymentDate: new Date('2026-01-02'),
        paymentMethod: 'credit_card',
        status: 'pending',
      });

      const found1 = await paymentRepository.findByPolicyId(policyId1, 0, 10);
      expect(found1).toHaveLength(1);
      expect(found1[0].policyId).toBe(policyId1);

      const found2 = await paymentRepository.findByPolicyId(policyId2, 0, 10);
      expect(found2).toHaveLength(1);
      expect(found2[0].policyId).toBe(policyId2);
    });
  });

  describe('countByPolicyIds', () => {
    it('should return counts for multiple policy ids', async () => {
      const policy1 = '550e8400-e29b-41d4-a716-446655440020';
      const policy2 = '550e8400-e29b-41d4-a716-446655440021';
      const policy3 = '550e8400-e29b-41d4-a716-446655440022';

      // Create payments for policy1 (3 payments)
      for (let i = 0; i < 3; i++) {
        await paymentModel.create({
          policyId: policy1,
          paymentAmount: 100,
          paymentDate: new Date(),
          paymentMethod: 'cash',
          status: 'completed',
        });
      }

      // Create payments for policy2 (2 payments)
      for (let i = 0; i < 2; i++) {
        await paymentModel.create({
          policyId: policy2,
          paymentAmount: 100,
          paymentDate: new Date(),
          paymentMethod: 'cash',
          status: 'completed',
        });
      }

      // No payments for policy3

      const counts = await paymentRepository.countByPolicyIds([
        policy1,
        policy2,
        policy3,
      ]);

      expect(counts[policy1]).toBe(3);
      expect(counts[policy2]).toBe(2);
      expect(counts[policy3]).toBe(0);
    });

    it('should return 0 for all ids when no payments exist', async () => {
      const policyIds = [
        '550e8400-e29b-41d4-a716-446655440030',
        '550e8400-e29b-41d4-a716-446655440031',
      ];

      const counts = await paymentRepository.countByPolicyIds(policyIds);

      expect(counts[policyIds[0]]).toBe(0);
      expect(counts[policyIds[1]]).toBe(0);
    });
  });
});
