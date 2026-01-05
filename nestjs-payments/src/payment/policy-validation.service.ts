import { HttpService } from '@nestjs/axios';
import { BadRequestException, Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { AxiosError } from 'axios';
import { firstValueFrom } from 'rxjs';

@Injectable()
export class PolicyValidationService {
  private readonly logger = new Logger(PolicyValidationService.name);
  private readonly policyServiceUrl: string;

  constructor(
    private readonly configService: ConfigService,
    private readonly httpService: HttpService,
  ) {
    this.policyServiceUrl =
      this.configService.getOrThrow<string>('POLICY_SERVICE_URL');
  }
  async assertExists(policyId: string): Promise<void> {
    const exists = await this.validatePolicyExists(policyId);
    if (!exists) {
      throw new BadRequestException(
        `Insurance policy with id ${policyId} doesn't exist`,
      );
    }
  }

  async validatePolicyExists(policyId: string): Promise<boolean> {
    try {
      const url = `${this.policyServiceUrl}/${policyId}`;
      this.logger.debug(`Validating policy existence: ${url}`);

      await firstValueFrom(
        this.httpService.get(url, {
          timeout: 5000,
        }),
      );

      this.logger.debug(`Policy ${policyId} exists`);
      return true;
    } catch (error) {
      if (error instanceof AxiosError) {
        if (error.response?.status === 404) {
          this.logger.debug(`Policy ${policyId} not found`);
          return false;
        }

        this.logger.warn(
          `Unexpected response status ${error.response?.status} for policy ${policyId}`,
        );
      }

      if (error instanceof Error)
        this.logger.error(
          `Error validating policy ${policyId}: ${error.message}`,
          error.stack,
        );
      throw error;
    }
  }
}
