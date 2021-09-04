package entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class Node {
     private int id;
     private double minFrequency;
     private double maxFrequency;
     private double curFrequency;
     private double accuracy;

     private double Pind;
     private double Cef;
     private double M;
}
