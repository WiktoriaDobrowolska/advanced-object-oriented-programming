package com.project.dao;

import com.project.datasource.DataSource;
import com.project.model.Zadanie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ZadanieDAOImpl implements ZadanieDAO {

    @Override
    public List<Zadanie> getZadania(Integer projektId) {
        List<Zadanie> zadania = new ArrayList<>();
        String sql = "SELECT zadanie_id, projekt_id, nazwa, opis, data FROM zadanie WHERE projekt_id = ? ORDER BY data";

        try (Connection connection = DataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, projektId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    zadania.add(new Zadanie(
                            rs.getInt("zadanie_id"),
                            rs.getInt("projekt_id"),
                            rs.getString("nazwa"),
                            rs.getString("opis"),
                            rs.getDate("data") != null ? rs.getDate("data").toLocalDate() : null
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return zadania;
    }

    @Override
    public void setZadanie(Zadanie zadanie) {
        boolean isInsert = zadanie.getZadanieId() == null;
        String sql = isInsert ?
                "INSERT INTO zadanie (projekt_id, nazwa, opis, data) VALUES (?, ?, ?, ?)" :
                "UPDATE zadanie SET projekt_id = ?, nazwa = ?, opis = ?, data = ? WHERE zadanie_id = ?";

        try (Connection connect = DataSource.getConnection();
             PreparedStatement pstmt = connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, zadanie.getProjektId());
            pstmt.setString(2, zadanie.getNazwa());
            pstmt.setString(3, zadanie.getOpis());
            pstmt.setDate(4, zadanie.getData() != null ? java.sql.Date.valueOf(zadanie.getData()) : null);

            if (!isInsert) {
                pstmt.setInt(5, zadanie.getZadanieId());
            }

            int affectedRows = pstmt.executeUpdate();

            if (isInsert && affectedRows > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        zadanie.setZadanieId(keys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteZadanie(Integer zadanieId) {
        String sql = "DELETE FROM zadanie WHERE zadanie_id = ?";
        try (Connection connection = DataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, zadanieId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
