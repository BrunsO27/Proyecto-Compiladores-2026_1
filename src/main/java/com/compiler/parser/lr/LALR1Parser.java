package com.compiler.parser.lr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.compiler.lexer.Token;
import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;

/**
 * Implements the LALR(1) parsing engine.
 * Uses a stack and the LALR(1) table to process a sequence of tokens.
 * Complementary task for Practice 9.
 */
public class LALR1Parser {
    private final LALR1Table table;

    public LALR1Parser(LALR1Table table) {
        this.table = table;
    }

   // package-private accessor for tests
   LALR1Table getTable() {
       return table;
   }

   /**
    * Parses a sequence of tokens using the LALR(1) parsing algorithm.
    * @param tokens The list of tokens from the lexer.
    * @return true if the sequence is accepted, false if a syntax error is found.
    */
   public boolean parse(List<Token> tokens) {
        // TODO: Implement the LALR(1) parsing algorithm.
        // 1. Initialize a stack for states and push the initial state (from table.getInitialState()).
        // 2. Create a mutable list of input tokens from the parameter and add the end-of-input token ("$").
        // 3. Initialize an instruction pointer `ip` to 0, pointing to the first token.
        // 4. Start a loop that runs until an ACCEPT or ERROR condition is met.
        //    a. Get the current state from the top of the stack.
        //    b. Get the current token `a` from the input list at index `ip`.
        //    c. Look up the action in the ACTION table: action = table.getActionTable()[state][a.type].
        //    d. If no action is found (it's null), it's a syntax error. Return false.
        //    e. If the action is SHIFT(s'):
        //       i. Push the new state s' onto the stack.
        //       ii. Advance the input pointer: ip++.
        //    f. If the action is REDUCE(A -> β):
        //       i. Pop |β| symbols (and states) from the stack. Handle epsilon productions (where |β|=0).
        //       ii. Get the new state `s` from the top of the stack.
        //       iii. Look up the GOTO state: goto_state = table.getGotoTable()[s][A].
        //       iv. If no GOTO state is found, it's an error. Return false.
        //       v. Push the goto_state onto the stack.
        //    g. If the action is ACCEPT:
        //       i. The input has been parsed successfully. Return true.
        //    h. If the action is none of the above, it's an unhandled case or error. Return false.

        Stack<Integer> stack = new Stack<>();
        stack.push(table.getInitialState());
        List<Token> input = new ArrayList<>(tokens);
        input.add(new Token("$", "$"));
        int ip = 0;
        Map<Integer, Map<Symbol, LALR1Table.Action>> actionTable = table.getActionTable();
        Map<Integer, Map<Symbol, Integer>> gotoTable = table.getGotoTable();

        while (true) {
            int state = stack.peek();
            Token a = input.get(ip);
            String symName = (a.type != null) ? a.type : a.lexeme;
            Symbol term = new Symbol(symName, SymbolType.TERMINAL);

            Map<Symbol, LALR1Table.Action> row = actionTable.getOrDefault(state, java.util.Collections.emptyMap());
            LALR1Table.Action act = row.get(term);

            if (act == null) {

                return false;
            }

            switch (act.type) {
                case SHIFT:
    
                    stack.push(act.state);
                    ip++;
                    break;

                case REDUCE:
    
                    Production prod = act.reduceProd;
                    int toPop = prod.getRight().size();
                    for (int i = 0; i < toPop; i++) {
                        if (stack.isEmpty()) return false;
                        stack.pop();
                    }
    
                    if (stack.isEmpty()) return false;
                    int s = stack.peek();
    
                    Integer gotoState = gotoTable.getOrDefault(s, java.util.Collections.emptyMap()).get(prod.getLeft());
                    if (gotoState == null) return false;
                    stack.push(gotoState);
                    break;

                case ACCEPT:
                    return true;

                default:
                    return false;
            }
        }
   }
}
