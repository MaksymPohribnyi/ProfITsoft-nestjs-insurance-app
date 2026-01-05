import { PaymentRepository } from './payment.repository';
import { PaymentService } from './payment.service';
import { PolicyValidationService } from './policy-validation.service';
import { PaymentCreateDto } from './dto/PaymentCreateDto';
import { BadRequestException } from '@nestjs/common';
import { PaymentQueryDto } from './dto/PaymentQueryDto';
import { CountsRequestDto } from './dto/CountsRequestDto';
import { plainToInstance } from 'class-transformer';

describe('PaymentService', () => {
  let service: PaymentService;
  let paymentRepository: PaymentRepository;
  let policyValidationService: PolicyValidationService;

  beforeEach(() => {
    paymentRepository = {
      create: jest.fn(),
      findByPolicyId: jest.fn(),
      countByPolicyIds: jest.fn(),
    } as unknown as PaymentRepository;

    policyValidationService = {
      assertExists: jest.fn(),
    } as unknown as PolicyValidationService;

    service = new PaymentService(paymentRepository, policyValidationService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
    expect(paymentRepository).toBeDefined();
    expect(policyValidationService).toBeDefined();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('create', () => {
    it('should create a payment when policy exists', async () => {
      const dto: PaymentCreateDto = plainToInstance(PaymentCreateDto, {
        policyId: '550e8400-e29b-41d4-a716-446655440000',
        paymentAmount: 1500,
        paymentMethod: 'credit_card',
      });

      const mockId = 'payment-id-123';

      jest
        .spyOn(policyValidationService, 'assertExists')
        .mockResolvedValue(undefined);
      jest.spyOn(paymentRepository, 'create').mockResolvedValue(mockId);

      const result = await service.create(dto);

      expect(result).toBe(mockId);
      expect(policyValidationService.assertExists).toHaveBeenCalledWith(
        dto.policyId,
      );
      expect(paymentRepository.create).toHaveBeenCalledWith(dto);
    });

    it('should throw BadRequestException when policy does not exist', async () => {
      const dto: PaymentCreateDto = plainToInstance(PaymentCreateDto, {
        policyId: '550e8400-e29b-41d4-a716-446655440000',
        paymentAmount: 1500,
        paymentMethod: 'credit_card',
      });

      const error = new BadRequestException(
        `Insurance policy with id ${dto.policyId} does not exist`,
      );
      jest
        .spyOn(policyValidationService, 'assertExists')
        .mockRejectedValue(error);

      await expect(service.create(dto)).rejects.toThrow(BadRequestException);
      await expect(service.create(dto)).rejects.toThrow(
        `Insurance policy with id ${dto.policyId} does not exist`,
      );
      expect(paymentRepository.create).not.toHaveBeenCalled();
    });

    it('should create payment with optional fields', async () => {
      const dto: PaymentCreateDto = plainToInstance(PaymentCreateDto, {
        policyId: '550e8400-e29b-41d4-a716-446655440001',
        paymentAmount: 2000,
        paymentMethod: 'cash',
        status: 'completed',
        description: 'Annual premium',
      });

      jest
        .spyOn(policyValidationService, 'assertExists')
        .mockResolvedValue(undefined);
      const mockId = 'payment-id-456';
      jest.spyOn(paymentRepository, 'create').mockResolvedValue(mockId);

      const result = await service.create(dto);

      expect(result).toBe(mockId);
      expect(paymentRepository.create).toHaveBeenCalledWith(dto);
    });
  });

  describe('list', () => {
    it('should return list of payments for a policy', async () => {
      const query: PaymentQueryDto = plainToInstance(PaymentQueryDto, {
        policyId: '550e8400-e29b-41d4-a716-446655440000',
        size: 10,
        from: 0,
      });

      const mockPayments = [
        {
          _id: 'payment-1',
          policyId: query.policyId,
          paymentAmount: 100,
          paymentDate: new Date('2026-01-01'),
          paymentMethod: 'cash',
          status: 'completed',
        },
        {
          _id: 'payment-2',
          policyId: query.policyId,
          paymentAmount: 200,
          paymentDate: new Date('2026-01-02'),
          paymentMethod: 'credit_card',
          status: 'pending',
        },
      ];

      jest
        .spyOn(paymentRepository, 'findByPolicyId')
        .mockResolvedValue(mockPayments);

      const result = await service.list(query);

      expect(result).toHaveLength(2);
      expect(paymentRepository.findByPolicyId).toHaveBeenCalledWith(
        query.policyId,
        query.from,
        query.size,
      );
      expect(result).toHaveLength(2);
      expect(result[0]).toHaveProperty('_id', 'payment-1');
      expect(result[0]).toHaveProperty('policyId', query.policyId);
      expect(result[0]).toHaveProperty('paymentAmount', 100);
      expect(result[1]).toHaveProperty('_id', 'payment-2');
    });

    it('should return empty array when no payments exist', async () => {
      const query: PaymentQueryDto = plainToInstance(PaymentQueryDto, {
        policyId: '550e8400-e29b-41d4-a716-446655440099',
        size: 10,
        from: 0,
      });

      jest.spyOn(paymentRepository, 'findByPolicyId').mockResolvedValue([]);

      const result = await service.list(query);

      expect(result).toEqual([]);
    });

    it('should use default pagination values', async () => {
      const query: PaymentQueryDto = plainToInstance(PaymentQueryDto, {
        policyId: '550e8400-e29b-41d4-a716-446655440000',
      });

      jest.spyOn(paymentRepository, 'findByPolicyId').mockResolvedValue([]);

      await service.list(query);

      expect(paymentRepository.findByPolicyId).toHaveBeenCalledWith(
        query.policyId,
        0,
        10,
      );
    });

    it('should handle custom pagination', async () => {
      const query: PaymentQueryDto = plainToInstance(PaymentQueryDto, {
        policyId: '550e8400-e29b-41d4-a716-446655440001',
        size: 5,
        from: 10,
      });

      jest.spyOn(paymentRepository, 'findByPolicyId').mockResolvedValue([]);

      await service.list(query);

      expect(paymentRepository.findByPolicyId).toHaveBeenCalledWith(
        query.policyId,
        10,
        5,
      );
    });
  });

  describe('getCounts', () => {
    it('should return counts for multiple policy ids', async () => {
      const dto: CountsRequestDto = plainToInstance(CountsRequestDto, {
        policyIds: [
          '550e8400-e29b-41d4-a716-446655440001',
          '550e8400-e29b-41d4-a716-446655440002',
          '550e8400-e29b-41d4-a716-446655440003',
        ],
      });

      const mockCounts = {
        '550e8400-e29b-41d4-a716-446655440001': 5,
        '550e8400-e29b-41d4-a716-446655440002': 3,
        '550e8400-e29b-41d4-a716-446655440003': 0,
      };

      jest
        .spyOn(paymentRepository, 'countByPolicyIds')
        .mockResolvedValue(mockCounts);

      const result = await service.getCount(dto);

      expect(result).toEqual(mockCounts);
      expect(paymentRepository.countByPolicyIds).toHaveBeenCalledWith(
        dto.policyIds,
      );
    });

    it('should return empty object for empty array', async () => {
      const dto: CountsRequestDto = plainToInstance(CountsRequestDto, {
        policyIds: [],
      });

      jest.spyOn(paymentRepository, 'countByPolicyIds').mockResolvedValue({});

      const result = await service.getCount(dto);

      expect(result).toEqual({});
    });

    it('should handle single policy id', async () => {
      const dto: CountsRequestDto = plainToInstance(CountsRequestDto, {
        policyIds: ['550e8400-e29b-41d4-a716-446655440001'],
      });

      const mockCounts = {
        '550e8400-e29b-41d4-a716-446655440001': 10,
      };

      jest
        .spyOn(paymentRepository, 'countByPolicyIds')
        .mockResolvedValue(mockCounts);

      const result = await service.getCount(dto);

      expect(result).toEqual(mockCounts);
      expect(paymentRepository.countByPolicyIds).toHaveBeenCalledWith(
        dto.policyIds,
      );
    });
  });
});
