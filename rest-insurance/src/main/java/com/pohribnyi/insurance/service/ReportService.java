package com.pohribnyi.insurance.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;
import com.pohribnyi.insurance.dto.request.PolicyPageableRequestDTO;
import com.pohribnyi.insurance.model.entity.Client;
import com.pohribnyi.insurance.model.entity.InsurancePolicy;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

	private final InsurancePolicyService policyService;

	public byte[] generateCsvReport(PolicyPageableRequestDTO request) throws IOException {
		
		List<InsurancePolicy> policies = policyService.getPoliciesForReport(request);

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(baos);
				CSVWriter writer = new CSVWriter(osw)) {

			String[] header = { "ID", "Policy Number", "Policy Type", "Start Date", "End Date", "Client Name",
					"Client Email" };
			writer.writeNext(header);

			for (InsurancePolicy policy : policies) {
				Client client = policy.getClient();
				String[] data = { 
						policy.getId().toString(), 
						policy.getPolicyNumber(), 
						policy.getPolicyType(),
						policy.getStartDate().toString(), 
						policy.getEndDate().toString(),
						client.getFirstName() + " " + client.getLastName(),
						client.getEmail() 
						};
				writer.writeNext(data);
			}
			
			writer.flush();
			return baos.toByteArray();
			
		}
	}
}