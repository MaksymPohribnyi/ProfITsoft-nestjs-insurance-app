import { IsArray, IsString } from 'class-validator';

export class CountsRequestDto {
  @IsArray()
  @IsString({ each: true })
  policyIds!: string[];
}
