package com.compiler.lexer.nfa;

import java.util.List;

/**
 * Represents a Non-deterministic Finite Automaton (NFA) with a start and end state.
 * <p>
 * An NFA is used in lexical analysis to model regular expressions and pattern matching.
 * This class encapsulates the start and end states of the automaton.
 */

public class NFA {
    /**
     * The initial (start) state of the NFA.
     */
    public final State startState;

    /**
     * The final (accepting) state of the NFA.
     */
    public final State endState;

    /**
     * Constructs a new NFA with the given start and end states.
     * @param start The initial state.
     * @param end The final (accepting) state.
     */
    public NFA(State start, State end) {
        // TODO: Implement constructor
        //throw new UnsupportedOperationException("Not implemented");
        startState = start;
        endState = end;
    }

    public static NFA union(List<NFA> nfas) {
        State newStart = new State();
        for (NFA nfa : nfas) {
            // Transición epsilon del nuevo estado inicial a cada NFA
            newStart.transitions.add(new com.compiler.lexer.nfa.Transition(null, nfa.startState));
        }
        // No hay un único estado final, pero cada NFA tiene su propio estado final marcado con TokenType
        // El NFA combinado usa el nuevo estado inicial y no necesita un estado final único
        return new NFA(newStart, null);
    }

    /**
     * Returns the initial (start) state of the NFA.
     * @return the start state
     */
    public State getStartState() {
    // TODO: Implement getStartState
    //throw new UnsupportedOperationException("Not implemented");
        return startState;
    }
}