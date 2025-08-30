package com.compiler.lexer.regex;

import java.util.List;
import java.util.Stack;

import com.compiler.lexer.nfa.*;

/**
 * RegexParser
 * -----------
 * This class provides functionality to convert infix regular expressions into nondeterministic finite automata (NFA)
 * using Thompson's construction algorithm. It supports standard regex operators: concatenation (·), union (|),
 * Kleene star (*), optional (?), and plus (+). The conversion process uses the Shunting Yard algorithm to transform
 * infix regex into postfix notation, then builds the corresponding NFA.
 *
 * Features:
 * - Parses infix regular expressions and converts them to NFA.
 * - Supports regex operators: concatenation, union, Kleene star, optional, plus.
 * - Implements Thompson's construction rules for NFA generation.
 *
 * Example usage:
 * <pre>
 *     RegexParser parser = new RegexParser();
 *     NFA nfa = parser.parse("a(b|c)*");
 * </pre>
 */
/**
 * Parses regular expressions and constructs NFAs using Thompson's construction.
 */
public class RegexParser {
    /**
     * Default constructor for RegexParser.
     */
        public RegexParser() {
            // TODO: Implement constructor if needed
        }

    /**
     * Converts an infix regular expression to an NFA.
     *
     * @param infixRegex The regular expression in infix notation.
     * @return The constructed NFA.
     */
    public NFA parse(String infixRegex) {
    // TODO: Implement parse
    // Pseudocode: Convert infix to postfix, then build NFA from postfix
    //throw new UnsupportedOperationException("Not implemented");
        String postfixString = ShuntingYard.toPostfix(infixRegex);
        return buildNfaFromPostfix(postfixString);
    }

    /**
     * Builds an NFA from a postfix regular expression.
     *
     * @param postfixRegex The regular expression in postfix notation.
     * @return The constructed NFA.
     */
    private NFA buildNfaFromPostfix(String postfixRegex) {
    // TODO: Implement buildNfaFromPostfix
    // Pseudocode: For each char in postfix, handle operators and operands using a stack
    //throw new UnsupportedOperationException("Not implemented");
        char[] charRegex = postfixRegex.toCharArray();
        Stack<NFA> stack = new Stack<NFA>();
        for (char c : charRegex) {
            if (c == '?') {
                this.handleOptional(stack);
            } else if (c == '+') {
                this.handlePlus(stack);
            } else if (isOperand(c)) {
                stack.push(this.createNfaForCharacter(c));
            } else if (c == '·') {
                this.handleConcatenation(stack);
            } else if (c == '|') {
                this.handleUnion(stack);
            } else if (c == '*') {
                this.handleKleeneStar(stack);
            } else {
                throw new IllegalArgumentException("Invalid character in regex: " + c);
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid postfix regex: " + postfixRegex);
        }

        return stack.pop();
    }

    /**
     * Handles the '?' operator (zero or one occurrence).
     * Pops an NFA from the stack and creates a new NFA that accepts zero or one occurrence.
     * @param stack The NFA stack.
     */
    private void handleOptional(Stack<NFA> stack) {
    // TODO: Implement handleOptional
    // Pseudocode: Pop NFA, create new start/end, add epsilon transitions for zero/one occurrence
    //throw new UnsupportedOperationException("Not implemented");
        NFA first = stack.pop();

        State startOptional = new State();
        State endOptional = new State();

        Transition tStartOpcional1 = new Transition(null, first.startState);
        Transition tStartOpcional2 = new Transition(null, endOptional);
        Transition tEndOpcional = new Transition(null, endOptional);

        startOptional.transitions.add(tStartOpcional1);
        startOptional.transitions.add(tStartOpcional2);

        first.endState.transitions.add(tEndOpcional);

        stack.push(new NFA(startOptional, endOptional));
    }

    /**
     * Handles the '+' operator (one or more occurrences).
     * Pops an NFA from the stack and creates a new NFA that accepts one or more occurrences.
     * @param stack The NFA stack.
     */
    private void handlePlus(Stack<NFA> stack) {
    // TODO: Implement handlePlus
    // Pseudocode: Pop NFA, create new start/end, add transitions for one or more occurrence
    //throw new UnsupportedOperationException("Not implemented");
        NFA first = stack.pop();

        State startPlus = new State();
        State endPlus = new State();

        Transition tStartPlus = new Transition(null, first.startState);
        Transition tEndPlus = new Transition(null, endPlus);

        Transition tFirst1 = new Transition(null, first.startState);

        startPlus.transitions.add(tStartPlus);

        first.endState.transitions.add(tFirst1);
        first.endState.transitions.add(tEndPlus);

        stack.push(new NFA(startPlus, endPlus));
    }
    
    /**
     * Creates an NFA for a single character.
     * @param c The character to create an NFA for.
     * @return The constructed NFA.
     */
    private NFA createNfaForCharacter(char c) {
    // TODO: Implement createNfaForCharacter
    // Pseudocode: Create start/end state, add transition for character
    //throw new UnsupportedOperationException("Not implemented");
        State start = new State();
        State end = new State();
        Transition t = new Transition(c, end);
        start.transitions.add(t);

        return new NFA(start, end);
    }

    /**
     * Handles the concatenation operator (·).
     * Pops two NFAs from the stack and connects them in sequence.
     * @param stack The NFA stack.
     */
    private void handleConcatenation(Stack<NFA> stack) {
    // TODO: Implement handleConcatenation
    // Pseudocode: Pop two NFAs, connect end of first to start of second
    //throw new UnsupportedOperationException("Not implemented");
        NFA second = stack.pop();
        NFA first = stack.pop();
        Transition t = new Transition(null, second.startState);
        first.endState.transitions.add(t);

        stack.push(new NFA(first.startState, second.endState));
    }

    /**
     * Handles the union operator (|).
     * Pops two NFAs from the stack and creates a new NFA that accepts either.
     * @param stack The NFA stack.
     */
    private void handleUnion(Stack<NFA> stack) {
    // TODO: Implement handleUnion
    // Pseudocode: Pop two NFAs, create new start/end, add epsilon transitions for union
    //throw new UnsupportedOperationException("Not implemented");
        NFA second = stack.pop();
        NFA first = stack.pop();

        State startUnion = new State();
        State endUnion = new State();

        Transition tStartFirst = new Transition(null, first.startState);
        Transition tStartSecond = new Transition(null, second.startState);

        startUnion.transitions.add(tStartFirst);
        startUnion.transitions.add(tStartSecond);

        Transition tEndFirst = new Transition(null, endUnion);
        Transition tEndSecnd = new Transition(null, endUnion);

        first.endState.transitions.add(tEndFirst);
        second.endState.transitions.add(tEndSecnd);

        stack.push(new NFA(startUnion, endUnion));
    }

    /**
     * Handles the Kleene star operator (*).
     * Pops an NFA from the stack and creates a new NFA that accepts zero or more repetitions.
     * @param stack The NFA stack.
     */
    private void handleKleeneStar(Stack<NFA> stack) {
    // TODO: Implement handleKleeneStar
    // Pseudocode: Pop NFA, create new start/end, add transitions for zero or more repetitions
    //throw new UnsupportedOperationException("Not implemented");

        NFA first = stack.pop();

        State kleeneStart = new State();
        State kleeneEnd = new State();

        Transition tStartKleene1 = new Transition(null, first.startState);
        Transition tStartKleene2 = new Transition(null, kleeneEnd);

        Transition tEndFirst1 = new Transition(null, kleeneEnd);
        Transition tEndFirst2 = new Transition(null, first.startState);

        kleeneStart.transitions.add(tStartKleene1);
        kleeneStart.transitions.add(tStartKleene2);
        
        first.endState.transitions.add(tEndFirst1);
        first.endState.transitions.add(tEndFirst2);

        stack.push(new NFA(kleeneStart, kleeneEnd));
    }

    /**
     * Checks if a character is an operand (not an operator).
     * @param c The character to check.
     * @return True if the character is an operand, false if it is an operator.
     */
    private boolean isOperand(char c) {
    // TODO: Implement isOperand
    // Pseudocode: Return true if c is not an operator
    //throw new UnsupportedOperationException("Not implemented");
        List<Character> lstOperators = List.of('|', '*', '?', '+', '(', ')', '·');

        if (!lstOperators.contains(c)) {
            return true;
        }

        return false;
    }
}