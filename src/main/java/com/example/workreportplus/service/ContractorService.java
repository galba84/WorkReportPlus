package com.example.workreportplus.service;

import com.example.jooq.tables.Contractor;
import com.example.workreportplus.dto.ContractorDto;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.example.jooq.Tables.CONTRACTOR;

@Service
public class ContractorService {

    private final DSLContext dsl;

    public ContractorService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public ContractorDto getContractorById(UUID id) {
        return dsl.selectFrom(Contractor.CONTRACTOR)
                .where(CONTRACTOR.ID.eq(id))
                .fetchAnyInto(ContractorDto.class);
    }

    public List<ContractorDto> getContractors() {
        return dsl.selectFrom(CONTRACTOR)
                .fetchInto(ContractorDto.class);
    }

}
