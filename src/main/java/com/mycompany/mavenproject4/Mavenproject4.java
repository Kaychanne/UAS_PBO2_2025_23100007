/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.mavenproject4;

/**
 *
 * @author ASUS
 */

import javax.swing.*;
import javax.swing.table.DefaultTableModel;


import java.awt.event.ActionEvent;

import java.awt.BorderLayout;

import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Mavenproject4 extends JFrame {

    private JTable visitTable;
    private DefaultTableModel tableModel;

    private JTextField nameField;
    private JTextField nimField;
    private JComboBox<String> studyProgramBox;
    private JComboBox<String> purposeBox;
    private JButton addButton;
    private JButton clearButton;
    private boolean modeUpdate = false;
    private String selectedDataId = null;
    private boolean actionColumnsAdded = false;

    public Mavenproject4() {
        setTitle("Library Visit Log");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        nameField = new JTextField();
        nimField = new JTextField();
        studyProgramBox = new JComboBox<>(new String[] {"Sistem dan Teknologi Informasi", "Bisnis Digital", "Kewirausahaan"});
        purposeBox = new JComboBox<>(new String[] {"Membaca", "Meminjam/Mengembalikan Buku", "Research", "Belajar"});
        addButton = new JButton("Add");
        clearButton = new JButton("Clear");

        inputPanel.setBorder(BorderFactory.createTitledBorder("Visit Entry Form"));
        inputPanel.add(new JLabel("NIM:"));
        inputPanel.add(nimField);
        inputPanel.add(new JLabel("Name Mahasiswa:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Program Studi:"));
        inputPanel.add(studyProgramBox);
        inputPanel.add(new JLabel("Tujuan Kunjungan:"));
        inputPanel.add(purposeBox);
        inputPanel.add(addButton);
        inputPanel.add(clearButton);

        add(inputPanel, BorderLayout.NORTH);

        String[] columns = {"Waktu Kunjungan", "NIM", "Nama", "Program Studi", "Tujuan Kunjungan"};
        tableModel = new DefaultTableModel(columns, 0);
        visitTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(visitTable);
        add(scrollPane, BorderLayout.CENTER);
        visitTable.getColumn("Update").setCellRenderer(new ButtonRenderer());
        visitTable.getColumn("Delete").setCellRenderer(new ButtonRenderer());
        // visitTable.getColumn("Update").setCellEditor(new ButtonEditor(new JCheckBox(), true));
        // visitTable.getColumn("Delete").setCellEditor(new ButtonEditor(new JCheckBox(), false));
        add(new JScrollPane(visitTable), BorderLayout.CENTER);

        addButton.addActionListener(e -> {
            if (modeUpdate) {
                updateData();
            } else {
                addData();
            }
        });
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        fetchAllDatas();
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("control G"), "showActions");

        getRootPane().getActionMap().put("showActions", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!actionColumnsAdded) {
                    addActionColumns();
                    actionColumnsAdded = true;
                }
            }
        });
    }
    
    private void addActionColumns() {
        tableModel.addColumn("Action");

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt("Action", i, tableModel.getColumnCount() - 2);
        }

        visitTable.getColumn("Action").setCellRenderer(new ButtonRenderer());

        visitTable.getColumn("Edit").setCellEditor(new ButtonEditor(new JCheckBox()));
    }

    private void addData(){
        try{
            String query = String.format(
                "mutation { addData(visitTime: \"%s\", studentId: \"%s\", studentName: %s, studyProgram: \"%s\", purpose: %s) { id studentId } }",
                nimField.getText(), nameField.getText(), studyProgramBox.getSelectedItem(), purposeBox.getSelectedItem()
            );
            sendGraphQLRequest(new GraphQLQuery(query));
            fetchAllDatas();
            clearInput();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Add error: " + e.getMessage());
        }
    }

    private void updateData() {
        try {
            String query = String.format(
                "mutation { studentId: \\\"%s\\\", studentName: %s, studyProgram: \\\"%s\\\", purpose: %s) { id } }",
                selectedDataId, nimField.getText(), nameField.getText(), studyProgramBox.getSelectedItem(), purposeBox.getSelectedItem()
            );
            sendGraphQLRequest(new GraphQLQuery(query));
            fetchAllDatas();
            clearInput();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Update error: " + e.getMessage());
        }
    }

    private void clearInput() {
        nimField.setText("");
        nameField.setText("");
        studyProgramBox.setSelectedItem("");
        purposeBox.setSelectedItem("");
        selectedDataId = null;
        addButton.setText("Add Data");
        modeUpdate = false;
    }

    private void fetchAllDatas() {
        try {
            String query = "query { allDatas { studentId studentName studyProgram purpose} }";
            String response = sendGraphQLRequest(new GraphQLQuery(query));
            JsonObject data = JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("data");
            JsonArray Datas = data.getAsJsonArray("allDatas");

            tableModel.setRowCount(0);
            for (JsonElement e : Datas) {
                JsonObject obj = e.getAsJsonObject();
                tableModel.addRow(new Object[]{
                    obj.get("studentid").getAsString(),
                    obj.get("studentName").getAsString(),
                    obj.get("studyProgram").getAsDouble(),
                    obj.get("purpose").getAsString(),
                    "Update",
                    "Delete"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Fetch error: " + e.getMessage());
        }
    }

    private String sendGraphQLRequest(GraphQLQuery gql) throws Exception {
        String json = new Gson().toJson(gql);
        URL url = new URL("http://localhost:4567/uaspbo");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }

        return sb.toString();
    }
    
    
    class GraphQLQuery {
        String query;
        GraphQLQuery(String query) {
            this.query = query;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Mavenproject4::new);
    }
}
