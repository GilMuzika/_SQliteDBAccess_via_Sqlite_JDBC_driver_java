/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package auxiliaryClasses;

import java.sql.SQLException;

/**
 *
 * @author gil_0
 */
public interface Action2<T1, T2> {
    void act(T1 val1, T2 val2) throws SQLException;
}
