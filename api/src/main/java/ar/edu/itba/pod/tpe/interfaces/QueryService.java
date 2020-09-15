package ar.edu.itba.pod.tpe.interfaces;

import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.exceptions.NoVotesException;
import ar.edu.itba.pod.tpe.models.Result;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface QueryService extends Remote {

    /**
     * Ask for partial/final results respectively for different dimensions
     * Export .csv for each query and print on stdout
     * If polls open -> partial with FPTP
     * If polls finished -> final with correspondent system
     * @throws IllegalElectionStateException if ask with polls closed.
     * @throws NoVotesException if no votes where found.
     * @throws RemoteException if any communication error occurs.
     */
    Result askNational() throws RemoteException, NoVotesException, IllegalElectionStateException;
    Result askState(String state) throws RemoteException, NoVotesException, IllegalElectionStateException;
    Result askTable(Integer table) throws RemoteException, NoVotesException, IllegalElectionStateException;
}

/*
Cliente sólo pregunta lo que quiere e imprime en pantalla el resultado + exporta csv con resultados de la consulta.
* */