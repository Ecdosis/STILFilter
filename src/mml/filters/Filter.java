/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mml.filters;
import org.json.simple.JSONObject;

/**
 * The Filter Interface
 * @author desmond
 */
public interface Filter {
    public JSONObject translate( JSONObject stilObj, byte[] text ) throws Exception;
}
