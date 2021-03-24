package com.boss.blueSpring.search.model.dao;

import static com.boss.blueSpring.common.JDBCTemplate.close;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.boss.blueSpring.board.model.vo.Board;
import com.boss.blueSpring.board.model.vo.PageInfo;

public class SearchDAO {
	private Statement stmt = null;
	private PreparedStatement pstmt = null;
	private ResultSet rset = null;
	
	
	/** 조건을 만족하는 게시글 수 조회 DAO
	 * @param conn
	 * @param condition
	 * @return listCount
	 * @throws Exception
	 */
	public int getListCount(Connection conn, String condition) throws Exception {
		int listCount = 0;
		
		String query = "SELECT COUNT(*) FROM V_BOARD WHERE BRD_DEL_FL = 'N' AND " + condition;
		
		try {
			stmt = conn.createStatement();
			
			rset = stmt.executeQuery(query);
			
			if(rset.next()) {
				listCount = rset.getInt(1);
			}
		} finally {
			close(rset);
			close(stmt);
		}
		
		return listCount;
	}


	/** 검색 게시글 목록 조회 DAO
	 * @param conn
	 * @param pInfo
	 * @param condition
	 * @return bList
	 * @throws Exception
	 */
	public List<Board> searchBoardList(Connection conn, PageInfo pInfo, String condition) throws Exception {
		List<Board> bList = null;
		
		String query = 
				"SELECT * FROM" + 
				"    (SELECT ROWNUM RNUM , V.*" + 
				"    FROM" + 
				"        (SELECT BRD_NO, BRD_TITLE, BRD_CONTENT, " + 
				"       MEM_ID, BRD_VIEWS, BRD_CRT_DT, BRD_UPDATE_DT, " + 
				"       CATEGORY_NM, BRD_DEL_FL, MEM_NICKNAME, (SELECT COUNT(*) FROM BOARD_LIKES LIKEBRD WHERE LIKEBRD.BRD_NO = VBRD.BRD_NO) LIKES FROM V_BOARD VBRD " + 
				"        WHERE " + condition + 
				"        AND BRD_DEL_FL = 'N' ORDER BY BRD_NO DESC) V )" + 
				"WHERE RNUM BETWEEN ? AND ?";

		try {
			// SQL 구문 조건절에 대입할 변수 생성
			int startRow = (pInfo.getCurrentPage() - 1) * pInfo.getLimit() + 1;
			int endRow = startRow + pInfo.getLimit() - 1;
			
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, startRow);
			pstmt.setInt(2, endRow);
			
			rset = pstmt.executeQuery();
			
			bList = new ArrayList<Board>();
			
			while(rset.next()) {
				Board board = new Board(rset.getInt("BRD_NO"), 
										rset.getString("BRD_TITLE"),
										rset.getString("MEM_ID"), 
										rset.getInt("BRD_VIEWS"),
										rset.getString("CATEGORY_NM"), 
										rset.getTimestamp("BRD_CRT_DT"),
										rset.getString("MEM_NICKNAME"),
										rset.getInt("LIKES"));
				bList.add(board);
			}
			
		} finally {
			close(rset);
			close(pstmt);
		}
		
		return bList;
	}

}
