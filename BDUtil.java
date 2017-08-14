package utilidades;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BDUtil {

    public PreparedStatement setValues(Connection conn, String sql, Object bean, String... gets) {
        Method[] methods = bean.getClass().getDeclaredMethods();
        ArrayList<Method> al = new ArrayList();
        for (int i = 0; i < gets.length; i++) {
            for (int j = 0; j < methods.length; j++) {
                if(gets[i].equals(methods[j].getName())){
                    al.add(methods[j]);
                    break;
                }
            }
        }
        Object[] data = new Object[al.size()];
        for (int i = 0; i < data.length; i++) {
            try {
                data[i] = al.get(i).invoke(bean);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(BDUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return setValues(conn, sql, data);
    }

    public PreparedStatement setValues(Connection conn, String sql, Object[] data) {
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            for (int i = 0, i2 = 1; i < data.length; i++) {
                String type = data[i].getClass().getName();
                switch (type) {
                    case "java.lang.String":
                        ps.setString((i2), (String) data[i]);
                        break;
                    case "java.lang.Char":
                        ps.setString((i2), ((Character) data[i]) + "");
                        break;
                    case "java.lang.Boolean":
                        ps.setBoolean((i2), (Boolean) data[i]);
                        break;
                    case "java.lang.Long":
                        ps.setLong((i2), (Long) data[i]);
                        break;
                    case "java.lang.Integer":
                        ps.setInt((i2), (Integer) data[i]);
                        break;
                    case "java.lang.Short":
                        ps.setShort((i2), (Short) data[i]);
                        break;
                    case "java.lang.Byte":
                        ps.setByte((i2), (Byte) data[i]);
                        break;
                    case "java.math.BigDecimal":
                        ps.setBigDecimal((i2), (BigDecimal) data[i]);
                        break;
                    case "java.lang.Double":
                        ps.setDouble((i2), (Double) data[i]);
                        break;
                    case "java.lang.Float":
                        ps.setFloat((i2), (Float) data[i]);
                        break;
                    case "java.time.LocalDate":
                        ps.setDate((i2), Date.valueOf((LocalDate) data[i]));
                        break;
                    case "java.time.LocalTime":
                        ps.setTime((i2), Time.valueOf((LocalTime) data[i]));
                        break;
                    case "java.time.LocalDateTime":
                        ps.setTimestamp((i2), Timestamp.valueOf((LocalDateTime) data[i]));
                        break;
                }
                i2++;
            }
            return ps;
        } catch (SQLException ex) {
            Logger.getLogger(BDUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public Object[][] getValues(ResultSet rs) throws SQLException {
        int cq = rs.getMetaData().getColumnCount();
        ArrayList[] al = new ArrayList[cq];
        while (rs.next()) {
            for (int i = 1; i <= cq; i++) {
                if (al[i - 1] == null) {
                    al[i - 1] = new ArrayList();
                }
                al[i - 1].add(rs.getObject(i));
            }
        }
        if (al[0] == null) {
            return null;
        }
        Object[][] vo = new Object[al[0].size()][cq];
        for (int i = 0; i < al[0].size(); i++) {
            for (int j = 0; j < cq; j++) {
                if(al[j].get(i) instanceof Date){
                    vo[i][j] = ((Date) al[j].get(i)).toLocalDate();
                    continue;
                }
                else if(al[j].get(i) instanceof Time){
                    vo[i][j] = ((Time) al[j].get(i)).toLocalTime();
                    continue;
                }
                else if(al[j].get(i) instanceof Timestamp){
                    vo[i][j] = ((Timestamp) al[j].get(i)).toLocalDateTime();
                    continue;
                }
                vo[i][j] = al[j].get(i);
            }
        }
        return vo;
    }

    public void getValues(ResultSet rs, Object bean, Object[] beansArray, String... sets) throws SQLException {
        Object[][] matriz = getValues(rs);
        Method[] mtds = bean.getClass().getMethods();
        ArrayList<Method> al = new ArrayList();
        for (int i = 0; i < sets.length; i++) {
            for (int j = 0; j < mtds.length; j++) {
                if(sets[i].equals(mtds[j].getName())){
                    al.add(mtds[j]);
                }
            }
        }
        Method[] setMtds = new Method[sets.length];
        for (int i = 0; i < sets.length; i++) {
            setMtds[i] = al.get(i);
        }
        beansArray = new Object[matriz.length];
        for (int i = 0; i < matriz.length; i++) {
            try {
                Object obj = bean.getClass().newInstance();
                for (int j = 0; j < sets.length; j++) {
                    if(matriz[i][j] == null){
                        continue;
                    }
                    setMtds[j].invoke(obj, matriz[i][j]);
                }
                beansArray[i] = obj;
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(BDUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void backup(Connection connIn, Connection connOut, String table, LocalDateTime id, String ID) throws SQLException{
        String sql = "SELECT * FROM " + table + " WHERE " + ID + " >= ?;";
        PreparedStatement psIn = connIn.prepareStatement(sql);
        psIn.setTimestamp(1, Timestamp.valueOf(id));
        ResultSet rsIn = psIn.executeQuery();
        //Em construção
    }
}
