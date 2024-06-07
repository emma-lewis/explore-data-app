package com.emmalewis.explore_data_app.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataAnalysisService {

    private List<CSVRecord> records;
    private List<String> headers;

    /**
     * Analyzes the uploaded CSV file and provides a summary of the data.
     * 
     * @param file the uploaded CSV file
     * @return a summary of the data
     * @throws Exception if an error occurs while processing the file
     */
    public String analyzeData(MultipartFile file) throws Exception {
        Reader reader = new InputStreamReader(file.getInputStream());
        CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                                               .setHeader()
                                               .setSkipHeaderRecord(true)
                                               .build();
        CSVParser parser = csvFormat.parse(reader);
        records = parser.getRecords();
        headers = parser.getHeaderNames();

        // Initialize statistics and counts
        Map<String, DescriptiveStatistics> stats = new HashMap<>();
        Map<String, Integer> naCounts = new HashMap<>();
        Map<String, Set<String>> uniqueValues = new HashMap<>();
        Map<String, Map<String, Integer>> valueCounts = new HashMap<>();

        // Process each record
        for (CSVRecord record : records) {
            for (String column : headers) {
                stats.putIfAbsent(column, new DescriptiveStatistics());
                uniqueValues.putIfAbsent(column, new HashSet<>());
                valueCounts.putIfAbsent(column, new HashMap<>());
                String value = record.get(column);

                if (value.isEmpty()) {
                    naCounts.put(column, naCounts.getOrDefault(column, 0) + 1);
                } else {
                    uniqueValues.get(column).add(value);
                    valueCounts.get(column).put(value, valueCounts.get(column).getOrDefault(value, 0) + 1);
                    try {
                        stats.get(column).addValue(Double.parseDouble(value));
                    } catch (NumberFormatException e) {
                        // Not a numeric value, continue
                    }
                }
            }
        }

        // Build summary
        StringBuilder summary = new StringBuilder();
        for (String column : stats.keySet()) {
            DescriptiveStatistics stat = stats.get(column);
            String type = stat.getN() > 0 ? "Numeric" : "Character";
            summary.append("Column: ").append(column).append("\n");
            summary.append("Type: ").append(type).append("\n");
            summary.append("Min: ").append(type.equals("Numeric") ? stat.getMin() : "N/A").append("\n");
            summary.append("Max: ").append(type.equals("Numeric") ? stat.getMax() : "N/A").append("\n");
            summary.append("Mean: ").append(type.equals("Numeric") ? stat.getMean() : "N/A").append("\n");
            summary.append("Median: ").append(type.equals("Numeric") ? stat.getPercentile(50) : "N/A").append("\n");
            summary.append("Mode: ").append(type.equals("Numeric") ? findMode(stat.getValues()) : "N/A").append("\n");
            summary.append("Standard Deviation: ").append(type.equals("Numeric") ? stat.getStandardDeviation() : "N/A").append("\n");
            summary.append("Variance: ").append(type.equals("Numeric") ? stat.getVariance() : "N/A").append("\n");
            summary.append("Count: ").append(stat.getN()).append("\n");
            summary.append("NA Values: ").append(naCounts.getOrDefault(column, 0)).append("\n");
            summary.append("Unique Values: ").append(uniqueValues.get(column).size()).append("\n");

            List<Map.Entry<String, Integer>> topValues = valueCounts.get(column).entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

            summary.append("Most Prevalent Values: ").append("\n");
            for (Map.Entry<String, Integer> entry : topValues) {
                summary.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }

            summary.append("\n");
        }

        return summary.toString();
    }

    /**
     * Returns the list of column headers.
     * 
     * @return the list of column headers
     */
    public List<String> getColumns() {
        return headers;
    }

    /**
     * Generates a visualization script for the specified column.
     * 
     * @param column the column to visualize
     * @return the visualization script
     */
    public String generateVisualization(String column) {
        List<Double> values = new ArrayList<>();
        boolean isNumeric = true;

        // Collect values for the column
        for (CSVRecord record : records) {
            String value = record.get(column);
            if (!value.isEmpty()) {
                try {
                    values.add(Double.parseDouble(value));
                } catch (NumberFormatException e) {
                    isNumeric = false;
                    break;
                }
            }
        }

        // Generate the appropriate visualization script
        if (isNumeric) {
            return generatePlotlyScript(column, values);
        } else {
            return "Non-numeric data";
        }
    }

    private String generatePlotlyScript(String column, List<Double> values) {
        StringBuilder script = new StringBuilder();
        script.append("<script>");
        script.append("var trace1 = {");
        script.append("x: ").append(values.toString()).append(",");
        script.append("type: 'histogram',");
        script.append("name: 'Histogram',");
        script.append("};");

        script.append("var trace2 = {");
        script.append("y: ").append(values.toString()).append(",");
        script.append("type: 'box',");
        script.append("name: 'Box Plot',");
        script.append("};");

        script.append("var data = [trace1, trace2];");
        script.append("var layout = {");
        script.append("title: 'Distribution of " + column + "',");
        script.append("xaxis: {title: '" + column + "'},");
        script.append("yaxis: {title: 'Value'},");
        script.append("};");

        script.append("Plotly.newPlot('visualization', data, layout);");
        script.append("</script>");
        return script.toString();
    }

    private boolean isNumericColumn(String column) {
        for (CSVRecord record : records) {
            String value = record.get(column);
            if (!value.isEmpty()) {
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        return true;
    }

    private String findMode(double[] values) {
        Map<Double, Long> frequency = Arrays.stream(values)
                                            .boxed()
                                            .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        return frequency.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .map(String::valueOf)
                        .orElse("N/A");
    }
}