package checkers.evaluation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import checkers.sandbox.Model;
import checkers.sandbox.SquareState;

public class TestMenCountEvaluation {

	private MenCountEvaluation eval;
	private Model m;

	@Before
	public void setup() {
		eval = new MenCountEvaluation();
		m = new Model();
		m.state = new SquareState[8][8];
		init(m);
	}

	@Test
	public void testEvaluateNull() {
		m.state = new SquareState[8][8];
		try{
			eval.evaluate(m, SquareState.WHITE);
			fail("Illegal State gelmeliydi");
		}catch(IllegalStateException exc){
			assertEquals(IllegalStateException.class, exc.getClass());
		}catch(Throwable t){
			fail("Illegal State gelmeliydi");
		}
	}

	@Test
	public void testEvaluateZero() {
		int value = eval.evaluate(m, SquareState.WHITE);
		assertEquals(0, value);
	}
	
	@Test
	public void testEvaluateOne() {
		m.state[2][2] = SquareState.WHITE;
		int value = eval.evaluate(m, SquareState.WHITE);
		assertEquals(1, value);
	}
	
	@Test
	public void testEvaluateOneBlack() {
		m.state[2][2] = SquareState.WHITE;
		int value = eval.evaluate(m, SquareState.BLACK);
		assertEquals(-1, value);
	}
	
	@Test
	public void testEvaluateAnyState() {
		m.state[2][2] = SquareState.WHITE;
		m.state[2][1] = SquareState.WHITE;
		m.state[1][2] = SquareState.KING_WHITE;
		m.state[2][4] = SquareState.WHITE;
		m.state[1][5] = SquareState.KING_WHITE;
		m.state[6][2] = SquareState.WHITE;
		m.state[7][2] = SquareState.KING_BLACK;
		m.state[5][2] = SquareState.BLACK;
		int value = eval.evaluate(m, SquareState.BLACK);
		assertEquals(-4, value);
		value = eval.evaluate(m, SquareState.WHITE);
		assertEquals(4, value);
	}


	private void init(Model m) {
		SquareState[][] state = m.state;
		for (int i = 0; i < state.length; i++) {
			for (int j = 0; j < state[i].length; j++) {
				state[i][j] = SquareState.BLANK;
			}
		}
	}
	
	
}