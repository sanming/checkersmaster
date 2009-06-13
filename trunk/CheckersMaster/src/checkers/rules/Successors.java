package checkers.rules;

import java.util.LinkedList;
import java.util.List;

import checkers.domain.Model;
import checkers.domain.Move;
import checkers.domain.Player;
import checkers.sandbox.SquareState;

public class Successors  implements ISuccessor{
	
	@Override
	public List<Move> getSuccessors(Model model, Player player) {
		LinkedList<Move> list = new LinkedList<Move>();
		SquareState[][] state = model.state;
		for (int i=0; i<state.length; i++){
			for (int j=0; j<state[i].length; j++){
				if(state[i][j].owner == player)
					handleStone(list, model, i, j);
			}
		}
		handleList(list);
		return list;
	}
	
//	@Override
//	public List<Move> getSuccessors(Model model, Player player) {
//		LinkedList<Move> list = new LinkedList<Move>();
//		SquareState[][] state = model.state;
//		for (int i=0; i<state.length; i++){
//			for (int j=0; j<state[i].length; j++){
//				canMoveThisStone(player, list, state, i, j);
//			}
//		}
//		
//		handleMust(model, player, list, new LinkedList<Move>());
//		
//		return list;
//	}
	
	private void canMoveThisStone(Player player, LinkedList<Move> list,
			SquareState[][] state, int i, int j) {
		SquareState square = state[i][j];
		switch (square) {
		case BLACK:
			if(player==Player.BLACK)
				handleBlackStone(list, state, i, j);
			break;
		case KING_BLACK:
			if(player==Player.BLACK)
				handleWhiteKingStone(list, state, i, j);
			break;
		case WHITE:
			if(player==Player.WHITE)
				handleWhiteStone(list, state, i, j);
			break;
		case KING_WHITE:
			if(player==Player.WHITE)
				handleWhiteKingStone(list, state, i, j);
			break;
		default:
			break;
		}
	}
	

	private boolean handleMust(Model model, Player player, LinkedList<Move> list, LinkedList<Move> tempList) {
		boolean hasMust = handleList(list);
		if(hasMust){
			for (Move move : list) {
				model.tryMove(move);
				LinkedList<Move> newList = new LinkedList<Move>();
				canMoveThisStone(player, newList, model.state, move.toX, move.toY);
				if(handleMust(model, player, newList, tempList)){
					move.nextMustMove = newList.get(0);
				}
				model.undoTryMove(move);
			}
		}
		return hasMust;
	}
	

	private boolean handleList(LinkedList<Move> list) {
		boolean hasMust = false;
		for (Move move : list) {
			if(move.must){
				hasMust = true;
				break;
			}
		}
		if(hasMust){
			LinkedList<Move> linkedList = new LinkedList<Move>(list);
			for (Move move : linkedList) {
				if(!move.must){
					list.remove(move);
				}
			}
		}
		return hasMust;
	}
	

	public void handleStone(LinkedList<Move> list, Model model, int i, int j){
		SquareState[][] state = model.state;
		if(state[i][j] == SquareState.BLANK)
			return;
		List<Move> eatList = eatMoveList(model, i, j);
		if(eatList != null && eatList.size()>0){
			list.addAll(eatList);
		}else{
			List<Move> moveList = normalMoveList(model, i, j);
			list.addAll(moveList);
		}
	}

	private List<Move> normalMoveList(Model model, int i, int j) {
		LinkedList<Move> moveList = new LinkedList<Move>();
		SquareState[][] state = model.state;
		SquareState currentSquare = state[i][j];
		Direction[] directions = getDirections(currentSquare);
		for (Direction direction : directions) {
			SquareState next = null;
			int toX = -1, toY = -1;
			switch (direction) {
				case RIGHT_BUTTOM:
					next = rightButtom(state, i, j);
					toX = i-1;
					toY = j+1;
					break;
				case RIGHT_FORWARD:
					next = rightForward(state, i, j);
					toX = i+1;
					toY = j+1;
					break;
				case LEFT_BUTTOM:
					next = leftButtom(state, i, j);
					toX = i-1;
					toY = j-1;
					break;
				case LEFT_FORWARD:
					next = leftForward(state, i, j);
					toX = i+1;
					toY = j-1;
					break;
				default: 
					throw new IllegalStateException("No default value");
			}
			boolean canGo = next==SquareState.BLANK;
			if(canGo){
				Move move = createNormalMove(i, j, toX, toY);
				moveList.add(move);
			}
		}
		
		return moveList;
	}

	//Gelen elemanın (i,j) bir taş olduğunu kabullenir.
	private List<Move> eatMoveList(Model model, int i, int j) {
		LinkedList<Move> eatList = new LinkedList<Move>();
		SquareState[][] state = model.state;
		SquareState currentSquare = state[i][j];
		Direction[] directions = getDirections(currentSquare);
		for (Direction direction : directions) {
			SquareState twoNext = null, next = null;
			int toX = -1, toY = -1;
			switch (direction) {
				case RIGHT_BUTTOM:
					twoNext = twoRightButtom(state, i, j);
					next = rightButtom(state, i, j);
					toX = i-2;
					toY = j+2;
					break;
				case RIGHT_FORWARD:
					twoNext = twoRightForward(state, i, j);
					next = rightForward(state, i, j);
					toX = i+2;
					toY = j+2;
					break;
				case LEFT_BUTTOM:
					twoNext = twoLeftButtom(state, i, j);
					next = leftButtom(state, i, j);
					toX = i-2;
					toY = j-2;
					break;
				case LEFT_FORWARD:
					twoNext = twoLeftForward(state, i, j);
					next = leftForward(state, i, j);
					toX = i+2;
					toY = j-2;
					break;
				default:
					throw new IllegalStateException("No default value");
			}
			if(twoNext != null && next != null){
				boolean canEat = next.owner != null && 
						next.owner != currentSquare.owner && 
						twoNext == SquareState.BLANK;
				if(canEat){
					Move move = createEatMove(i, j, toX, toY);
					model.tryMove(move);
					List<Move> nextList = eatMoveList(model, toX, toY);
					if(nextList != null && nextList.size()>0){
						for (Move nextMove : nextList) {
							Move currentMove = createEatMove(i, j, toX, toY);
							currentMove.nextMustMove = nextMove;
							//currentMove.convert = move.convert;
							eatList.add(currentMove);
						}
					}else{
						Move currentMove = createEatMove(i, j, toX, toY);
						currentMove.nextMustMove = null;
						//currentMove.convert = move.convert;
						eatList.add(currentMove);						
					}
					model.undoTryMove(move);
				}
			}
		}
		return eatList;
	}

	private Move createNormalMove(int i, int j, int toX, int toY) {
		Move move = createMove(i, j, toX, toY);
		move.must = false;
		return move;
	}

	private Move createEatMove(int i, int j, int toX, int toY) {
		Move move = createMove(i, j, toX, toY);
		move.must = true;
		return move;
	}

	private Move createMove(int i, int j, int toX, int toY) {
		Move move = new Move();
		move.fromX = i;
		move.fromY = j;
		move.toX = toX;
		move.toY = toY;
		return move;
	}
	
	private Direction[] getDirections(SquareState squareState) {
		if(squareState.owner == null)
			return null;
		if(squareState.isKing()){
			return kingDirections;
		}
		if(squareState.owner == Player.WHITE){
			return whiteDirections;
		}
		if(squareState.owner == Player.BLACK){
			return blackDirections;
		}
		throw new IllegalStateException("bu durum gerçekleşemez!");
	}

	enum Direction{
		RIGHT_FORWARD,
		LEFT_FORWARD,
		RIGHT_BUTTOM,
		LEFT_BUTTOM
	}
	
	private final static Direction[] kingDirections = Direction.values();
	private final static Direction[] whiteDirections = new Direction[]{Direction.RIGHT_FORWARD, Direction.LEFT_FORWARD};
	private final static Direction[] blackDirections = new Direction[]{Direction.RIGHT_BUTTOM, Direction.LEFT_BUTTOM};
	
	private void handleWhiteStone(LinkedList<Move> list, SquareState[][] state, int i, int j) {
		//basit bir ta� sadece ileriye gidebilir...
		if(forwardCheck(state, i)){
			throw new IllegalStateException();
		}
		SquareState currentSquare = state[i][j];
		SquareState rightForward = rightForward(state,i,j);
		if(rightForward != null){
			if(rightForward.owner == null){
				//gidebilir:
				Move move = new Move();
				move.fromX=i;
				move.fromY=j;
				move.toX = i+1;
				move.toY = j+1;
				list.add(move);
			}else if(rightForward.owner == currentSquare.owner){
				//gidemez
			}else {
				//yerse gidebilir...
				SquareState twoRightForward = twoRightForward(state,i,j);
				if(twoRightForward == SquareState.BLANK){
					Move move = new Move();
					move.fromX=i;
					move.fromY=j;
					move.toX = i+2;
					move.toY = j+2;
					list.add(move);
					move.must = true;
				}
			}
		}
		SquareState leftForward = leftForward(state,i,j);
		if(leftForward != null){
			if(leftForward.owner == null){
				//gidebilir:
				Move move = new Move();
				move.fromX=i;
				move.fromY=j;
				move.toX = i+1;
				move.toY = j-1;
				list.add(move);
			}else if(leftForward.owner == currentSquare.owner){
				//gidemez
			}else {
				//yerse gidebilir...
				SquareState twoLeftForward = twoLeftForward(state,i,j);
				if(twoLeftForward == SquareState.BLANK){
					Move move = new Move();
					move.fromX=i;
					move.fromY=j;
					move.toX = i+2;
					move.toY = j-2;
					list.add(move);
					move.must = true;
				}
			}
		}
	}

	private void handleWhiteKingStone(LinkedList<Move> list, SquareState[][] state, int i, int j) {
		SquareState currentSquare = state[i][j];
		if(!forwardCheck(state, i)){
			SquareState rightForward = rightForward(state,i,j);
			if(rightForward != null){
				if(rightForward.owner == null){
					//gidebilir:
					Move move = new Move();
					move.fromX=i;
					move.fromY=j;
					move.toX = i+1;
					move.toY = j+1;
					list.add(move);
				}else if(rightForward.owner == currentSquare.owner){
					//gidemez
				}else {
					//yerse gidebilir...
					SquareState twoRightForward = twoRightForward(state,i,j);
					if(twoRightForward == SquareState.BLANK){
						Move move = new Move();
						move.fromX=i;
						move.fromY=j;
						move.toX = i+2;
						move.toY = j+2;
						list.add(move);
						move.must = true;
					}
				}
			}
			SquareState leftForward = leftForward(state,i,j);
			if(leftForward != null){
				if(leftForward.owner == null){
					//gidebilir:
					Move move = new Move();
					move.fromX=i;
					move.fromY=j;
					move.toX = i+1;
					move.toY = j-1;
					list.add(move);
				}else if(leftForward.owner == currentSquare.owner){
					//gidemez
				}else {
					//yerse gidebilir...
					SquareState twoLeftForward = twoLeftForward(state,i,j);
					if(twoLeftForward == SquareState.BLANK){
						Move move = new Move();
						move.fromX=i;
						move.fromY=j;
						move.toX = i+2;
						move.toY = j-2;
						list.add(move);
						move.must = true;
					}
				}
			}
		}
		if(!buttomCheck(state, i)){
			SquareState rightButtom = rightButtom(state,i,j);
			if(rightButtom != null){
				if(rightButtom.owner == null){
					//gidebilir:
					Move move = new Move();
					move.fromX=i;
					move.fromY=j;
					move.toX = i-1;
					move.toY = j+1;
					list.add(move);
				}else if(rightButtom.owner == currentSquare.owner){
					//gidemez
				}else {
					//yerse gidebilir...
					SquareState twoRightButtom = twoRightButtom(state,i,j);
					if(twoRightButtom == SquareState.BLANK){
						Move move = new Move();
						move.fromX=i;
						move.fromY=j;
						move.toX = i-2;
						move.toY = j+2;
						list.add(move);
						move.must = true;
					}
				}
			}
			SquareState leftButtom = leftButtom(state,i,j);
			if(leftButtom != null){
				if(leftButtom.owner == null){
					//gidebilir:
					Move move = new Move();
					move.fromX=i;
					move.fromY=j;
					move.toX = i-1;
					move.toY = j-1;
					list.add(move);
				}else if(leftButtom.owner == currentSquare.owner){
					//gidemez
				}else {
					//yerse gidebilir...
					SquareState twoLeftButtom = twoLeftButtom(state,i,j);
					if(twoLeftButtom == SquareState.BLANK){
						Move move = new Move();
						move.fromX=i;
						move.fromY=j;
						move.toX = i-2;
						move.toY = j-2;
						list.add(move);
						move.must = true;
					}
				}
			}
		}
	}
	

	private void handleBlackStone(LinkedList<Move> list, SquareState[][] state, int i, int j) {
		if(buttomCheck(state, i)){
			throw new IllegalStateException();
		}
		SquareState currentSquare = state[i][j];
		SquareState rightButtom = rightButtom(state,i,j);
		if(rightButtom != null){
			if(rightButtom.owner == null){
				//gidebilir:
				Move move = new Move();
				move.fromX=i;
				move.fromY=j;
				move.toX = i-1;
				move.toY = j+1;
				list.add(move);
			}else if(rightButtom.owner == currentSquare.owner){
				//gidemez
			}else {
				//yerse gidebilir...
				SquareState twoRightButtom = twoRightButtom(state,i,j);
				if(twoRightButtom == SquareState.BLANK){
					Move move = new Move();
					move.fromX=i;
					move.fromY=j;
					move.toX = i-2;
					move.toY = j+2;
					list.add(move);
					move.must = true;
				}
			}
		}
		SquareState leftButtom = leftButtom(state,i,j);
		if(leftButtom != null){
			if(leftButtom.owner == null){
				//gidebilir:
				Move move = new Move();
				move.fromX=i;
				move.fromY=j;
				move.toX = i-1;
				move.toY = j-1;
				list.add(move);
			}else if(leftButtom.owner == currentSquare.owner){
				//gidemez
			}else {
				//yerse gidebilir...
				SquareState twoLeftButtom = twoLeftButtom(state,i,j);
				if(twoLeftButtom == SquareState.BLANK){
					Move move = new Move();
					move.fromX=i;
					move.fromY=j;
					move.toX = i-2;
					move.toY = j-2;
					list.add(move);
					move.must = true;
				}
			}
		}
	}
	
	//iki sol altın değerini getirir
	private SquareState twoLeftButtom(SquareState[][] state, int i, int j) {
		if(i-2>=0 &&  j-2 >= 0){
			return state[i-2][j-2];
		}
		return null;
	}

	//sol altın değerini getirir
	private SquareState leftButtom(SquareState[][] state, int i, int j) {
		if(i-1>=0 &&  j-1 >= 0){
			return state[i-1][j-1];
		}
		return null;
	}

	//iki sağ altın değerini getirir
	private SquareState twoRightButtom(SquareState[][] state, int i, int j) {
		if(buttomCheck(state, i)){
			return null;
		}
		if( i-2 >= 0 && j+2 < state[i].length){
			return state[i-2][j+2];
		}
		return null;
	}

	//sağ altın değerini getirir
	private SquareState rightButtom(SquareState[][] state, int i, int j) {
		if(buttomCheck(state, i)){
			return null;
		}
		if(i-1>=0 &&  j+1 < state[i].length){
			return state[i-1][j+1];
		}
		return null;
	}

	private SquareState twoRightForward(SquareState[][] state, int i, int j) {
		if( i+2 < state.length && j+2 < state[i].length){
			return state[i+2][j+2];
		}
		return null;
	}
	
	private SquareState twoLeftForward(SquareState[][] state, int i, int j) {
		if( i+2 < state.length && j-2 >= 0){
			return state[i+2][j-2];
		}
		return null;
	}


	//sol önün değerini getirir..
	private SquareState leftForward(SquareState[][] state, int i, int j) {
		if(forwardCheck(state, i)){
			return null;
		}
		if(j-1 >= 0){
			return state[i+1][j-1];
		}
		return null;
	}
	
	//sağ önün değerini getirir..
	private SquareState rightForward(SquareState[][] state, int i, int j) {
		if(forwardCheck(state, i)){
			return null;
		}			
		if(j+1 < state[i].length){
			return state[i+1][j+1];
		}
		return null;
	}

	//ileriye gidebilir mi?
	private boolean forwardCheck(SquareState[][] state, int i) {
		return i+1 >= state.length;
	}
	
	//ileriye gidebilir mi?
	private boolean buttomCheck(SquareState[][] state, int i) {
		return i-1 < 0;
	}
}