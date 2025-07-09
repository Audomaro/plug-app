package com.example.plugins.output.txtfile.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data()
@ToString()
@AllArgsConstructor()
@NoArgsConstructor()
public class FileOutputConfig {
    private String filePath = "output.txt";
}
