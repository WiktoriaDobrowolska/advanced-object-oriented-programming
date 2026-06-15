package com.project.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.project.datasource.DataSource;
import com.project.model.Projekt;

public class ProjektDAOImpl implements ProjektDAO {

    @Override
    public List<Projekt> getProjekty(String search, Integer offset, Integer limit) {
        List<Projekt> projekty = new ArrayList<>();
        String sql = "SELECT projekt_id, nazwa, opis, dataczas_utworzenia, data_oddania " +
                "FROM projekt " +
                "WHERE LOWER(nazwa) LIKE LOWER(?) " +
                "ORDER BY projekt_id LIMIT ? OFFSET ?";

        try (java.sql.Connection connection = com.project.datasource.DataSource.getConnection();
             java.sql.PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, "%" + search + "%");
            pstmt.setInt(2, limit);
            pstmt.setInt(3, offset);

            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    projekty.add(new Projekt(
                            rs.getInt("projekt_id"),
                            rs.getString("nazwa"),
                            rs.getString("opis"),
                            rs.getTimestamp("dataczas_utworzenia") != null ? rs.getTimestamp("dataczas_utworzenia").toLocalDateTime() : null,
                            rs.getDate("data_oddania") != null ? rs.getDate("data_oddania").toLocalDate() : null
                    ));
                }
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Błąd pobierania projektów z filtrowaniem", e);
        }
        return projekty;
    }

    @Override
    public void setProjekt(Projekt projekt) {
        boolean isInsert = projekt.getProjektId() == null;
        String query = isInsert ?
                "INSERT INTO projekt (nazwa, opis, dataczas_utworzenia, data_oddania) VALUES (?, ?, ?, ?)"
                : "UPDATE projekt SET nazwa = ?, opis = ?, dataczas_utworzenia = ?, data_oddania = ? WHERE projekt_id = ?";

        try (Connection connect = DataSource.getConnection();
             PreparedStatement prepStmt = connect.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            prepStmt.setString(1, projekt.getNazwa());
            prepStmt.setString(2, projekt.getOpis());

            if (projekt.getDataCzasUtworzenia() == null) {
                projekt.setDataCzasUtworzenia(LocalDateTime.now());
            }
            prepStmt.setObject(3, projekt.getDataCzasUtworzenia());
            prepStmt.setObject(4, projekt.getDataOddania());

            if (!isInsert) {
                prepStmt.setInt(5, projekt.getProjektId());
            }

            int liczbaDodanychWierszy = prepStmt.executeUpdate();

            if (isInsert && liczbaDodanychWierszy > 0) {
                ResultSet keys = prepStmt.getGeneratedKeys();
                if (keys.next()) {
                    projekt.setProjektId(keys.getInt(1));
                }
                keys.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteProjekt(Integer projektId) {
        String query = "DELETE FROM projekt WHERE projekt_id = ?";
        try (Connection connect = DataSource.getConnection();
             PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setInt(1, projektId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Projekt> getProjekty(Integer offset, Integer limit) {
        List<Projekt> projekty = new ArrayList<>();
        String query = "SELECT * FROM projekt ORDER BY dataczas_utworzenia DESC"
                + (offset != null ? " OFFSET ?" : "")
                + (limit != null ? " LIMIT ?" : "");

        try (Connection connect = DataSource.getConnection();
             PreparedStatement preparedStmt = connect.prepareStatement(query)) {

            int i = 1;
            if (offset != null) {
                preparedStmt.setInt(i++, offset);
            }
            if (limit != null) {
                preparedStmt.setInt(i++, limit);
            }

            try (ResultSet rs = preparedStmt.executeQuery()) {
                while (rs.next()) {
                    Projekt projekt = new Projekt();
                    projekt.setProjektId(rs.getInt("projekt_id"));
                    projekt.setNazwa(rs.getString("nazwa"));
                    projekt.setOpis(rs.getString("opis"));
                    projekt.setDataCzasUtworzenia(rs.getObject("dataczas_utworzenia", LocalDateTime.class));
                    projekt.setDataOddania(rs.getObject("data_oddania", LocalDate.class));
                    projekty.add(projekt);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return projekty;
    }

    @Override
    public List<Projekt> getProjektyWhereNazwaLike(String nazwa, Integer offset, Integer limit) {
        List<Projekt> projekty = new ArrayList<>();
        String query = "SELECT * FROM projekt WHERE lower(nazwa) LIKE lower(?) ORDER BY dataczas_utworzenia DESC"
                + (offset != null ? " OFFSET ?" : "")
                + (limit != null ? " LIMIT ?" : "");

        try (Connection connect = DataSource.getConnection();
             PreparedStatement preparedStmt = connect.prepareStatement(query)) {

            int i = 1;
            preparedStmt.setString(i++, "%" + nazwa + "%");

            if (offset != null) {
                preparedStmt.setInt(i++, offset);
            }
            if (limit != null) {
                preparedStmt.setInt(i++, limit);
            }

            try (ResultSet rs = preparedStmt.executeQuery()) {
                while (rs.next()) {
                    Projekt projekt = new Projekt();
                    projekt.setProjektId(rs.getInt("projekt_id"));
                    projekt.setNazwa(rs.getString("nazwa"));
                    projekt.setOpis(rs.getString("opis"));
                    projekt.setDataCzasUtworzenia(rs.getObject("dataczas_utworzenia", LocalDateTime.class));
                    projekt.setDataOddania(rs.getObject("data_oddania", LocalDate.class));
                    projekty.add(projekt);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return projekty;
    }

    @Override
    public List<Projekt> getProjektyWhereDataOddaniaIs(LocalDate dataOddania, Integer offset, Integer limit) {
        List<Projekt> projekty = new ArrayList<>();
        String query = "SELECT * FROM projekt WHERE data_oddania = ? ORDER BY dataczas_utworzenia DESC"
                + (offset != null ? " OFFSET ?" : "")
                + (limit != null ? " LIMIT ?" : "");

        try (Connection connect = DataSource.getConnection();
             PreparedStatement preparedStmt = connect.prepareStatement(query)) {

            int i = 1;
            preparedStmt.setObject(i++, dataOddania);

            if (offset != null) {
                preparedStmt.setInt(i++, offset);
            }
            if (limit != null) {
                preparedStmt.setInt(i++, limit);
            }

            try (ResultSet rs = preparedStmt.executeQuery()) {
                while (rs.next()) {
                    Projekt projekt = new Projekt();
                    projekt.setProjektId(rs.getInt("projekt_id"));
                    projekt.setNazwa(rs.getString("nazwa"));
                    projekt.setOpis(rs.getString("opis"));
                    projekt.setDataCzasUtworzenia(rs.getObject("dataczas_utworzenia", LocalDateTime.class));
                    projekt.setDataOddania(rs.getObject("data_oddania", LocalDate.class));
                    projekty.add(projekt);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return projekty;
    }

    @Override
    public int getRowsNumber() {
        String query = "SELECT COUNT(*) FROM projekt";
        try (Connection connect = DataSource.getConnection();
             Statement stmt = connect.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public int getRowsNumberWhereNazwaLike(String nazwa) {
        String query = "SELECT COUNT(*) FROM projekt WHERE lower(nazwa) LIKE lower(?)";
        try (Connection connect = DataSource.getConnection();
             PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setString(1, "%" + nazwa + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public int getRowsNumberWhereDataOddaniaIs(LocalDate dataOddania) {
        String query = "SELECT COUNT(*) FROM projekt WHERE data_oddania = ?";
        try (Connection connect = DataSource.getConnection();
             PreparedStatement stmt = connect.prepareStatement(query)) {
            stmt.setObject(1, dataOddania);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }


}