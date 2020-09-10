package ar.edu.itba.pod.tpe.interfaces;

import ar.edu.itba.pod.tpe.exceptions.QueryException;
import ar.edu.itba.pod.tpe.models.Result;
import ar.edu.itba.pod.tpe.models.Vote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface QueryService extends Remote {

    /**
     * Ask for partial/final results respectively for different dimensions
     * Export .csv for each query and print on stdout
     * If polls open -> partial with FPTP
     * If polls finished -> final with correspondent system
     * @throws QueryException if ask with polls closed.
     * @throws RemoteException if any communication error occurs.
     */
    Result askNational() throws RemoteException, QueryException;
    Result askState(String state) throws RemoteException, QueryException;
    Result askTable(Integer table) throws RemoteException, QueryException;
}

/*
Cliente sólo pregunta lo que quiere e imprime en pantalla el resultado + exporta csv con resultados de la consulta.
* */