/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.pupin.jpo.validation;

/**
 *
 * @author vukm
 */
public interface QueryEvaluator {
    public static final int New=0, Running=1, Cancelled=2, Finished=3;
    
    public void evaluate(String query, Runnable postRun);
    public void evaluate(String query);
    public void cancel();
    public int getStatus();
}
