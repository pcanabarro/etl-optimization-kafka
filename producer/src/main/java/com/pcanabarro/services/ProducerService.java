package com.pcanabarro.services;

import com.pcanabarro.entities.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProducerService {
    private final DatabaseService databaseService;
    private final KafkaService kafkaService;

    @Autowired
    public ProducerService(DatabaseService databaseService, KafkaService kafkaService) {
        this.databaseService = databaseService;
        this.kafkaService = kafkaService;
    }

    public int sendMessages() {
        try {
            List<Employee> records = databaseService.fetchEmployeeRecords();
            int i = 1;
            for (Employee record : records) {
                kafkaService.sendMessage("etl_topic", Integer.toString(i), record.toString());
                System.out.println("Enviado: " + record);
                i++;
            }
            return records.size();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
