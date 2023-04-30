/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.example;

import java.sql.SQLException;

/**
 *
 * THis interface must have just one generic parameter, for result,
 * because it needs to be defined as a second generic parameter for HashMap,
 * so it can be defined there either without its own generic parameters at all, or all the parameters must dbe declared,
 * but the output type would vary, so it must be declared without parameters
 */
public interface IGetField<Trezult> {
    Trezult getData(String val) throws SQLException;
}
