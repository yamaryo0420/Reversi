import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Reversi extends JPanel{
    public final static int UNIT_SIZE = 80;
    Board board = new Board();
    private int turn; //1:Stone.black 2:Stone.white
    private Player[] player = new Player[2];
    public static int tactics;

    //コンストラクタの初期化
    public Reversi(){
        setPreferredSize(new Dimension(UNIT_SIZE*10, UNIT_SIZE*10)); //パネルサイズを800x800に設定
        addMouseListener(new MouseProc());
        player[0] = new Player(Stone.black, Player.type_human);
        player[1] = new Player(Stone.white, Player.type_computer);
    }

    //画面描画
    public void paintComponent(Graphics g){
        board.paint(g, UNIT_SIZE);
        //画面メッセージ
        String msg1 = "";
        g.setColor(Color.white);
        if(turn == Stone.white) msg1 = "白の番です";
        else{
            turn = Stone.black; //初ターン
            msg1 = "黒の番です";
        }
        if(player[turn-1].getType() == Player.type_computer) msg1 += "(考えています)";

        String msg2 = "[黒:" + board.countStone(Stone.black) + ", 白:" + board.countStone(Stone.white) + "]";
        g.drawString(msg1, UNIT_SIZE/2, UNIT_SIZE/2);
        g.drawString(msg2, UNIT_SIZE/2, 19*UNIT_SIZE/2);
    }

    //起動
    public static void main(String[] args) {
        tactics = Integer.parseInt(args[0]);// コンピュータのレベルを取得
        JFrame f = new JFrame();
        f.getContentPane().setLayout(new FlowLayout());
        f.getContentPane().add(new Reversi());
        f.pack();
        f.setResizable(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    // 終了メッセージダイアログの表示
    void EndMessageDialog() {
        int black = board.countStone(Stone.black);
        int white = board.countStone(Stone.white);
        String str = "[黒:" + black + ",白:" + white + "]で";

        if(black > white) str += "黒の勝ち";
        else if(black < white) str += "白の勝ち";
        else str += "引き分け";

        JOptionPane.showMessageDialog(this, str, "ゲーム終了", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    // メッセージダイアログを表示
    void MessageDialog(String str){
        JOptionPane.showMessageDialog(this, str, "情報", JOptionPane.INFORMATION_MESSAGE);
    }

    //手番を管理
    void changeTurn(){
        if(turn == Stone.black) turn = Stone.white;
        else if(turn == Stone.white) turn = Stone.black;
    }

    //クリックされたときの処理用のクラス
    class MouseProc extends MouseAdapter {
        public void mouseClicked(MouseEvent me) {
            Point point = me.getPoint();
            int btn = me.getButton();
            Point gp = new Point();
            gp.x = point.x / UNIT_SIZE - 1;
            gp.y = point.y / UNIT_SIZE - 1;
            
            if(!board.isOnBoard(gp.x, gp.y)) return; //盤面の外
            removeMouseListener(this); //一時的にマウスイベントの処理を止める
            board.evaluateBoard(); //ターン前の評価
            //プレイヤー（人間）の手番
            if(player[turn-1].getType() == Player.type_human) {
                if((player[turn-1].getColor() == Stone.black && board.num_grid_black == 0) || (player[turn-1].getColor() == Stone.white && board.num_grid_white == 0)){
                    MessageDialog("あなたはパスです");
                    changeTurn();
                    repaint();
                }
                else if((player[turn-1].getColor() == Stone.black && board.eval_black[gp.x][gp.y] > 0 && btn == MouseEvent.BUTTON1) || (player[turn-1].getColor() == Stone.white && board.eval_white[gp.x][gp.y] > 0 && btn == MouseEvent.BUTTON3)) {
                    Point nm = player[turn-1].nextMove(board, gp, tactics);
                    board.setStoneAndReverse(nm.x, nm.y, player[turn-1].getColor());
                    changeTurn();
                    board.evaluateBoard(); //ターン後の評価
                    // board.printEval();
                    repaint();
                    //ゲーム終了確認
                    if(board.num_grid_black == 0 && board.num_grid_white == 0) EndMessageDialog();
                }
                //人間対人間の時
                if(player[turn-1].getType() == Player.type_human) addMouseListener(this);
            }
            //コンピュータ（自動）の手番
            if(player[turn-1].getType() == Player.type_computer){
                Thread th = new TacticsThread();
                th.start();
            }
        }
    }
    //コンピュータとの対戦用スレッド
    class TacticsThread extends Thread {
        public void run(){
            try{
                Thread.sleep(2000); //2秒間待つ
                Point nm = player[turn-1].nextMove(board, new Point(-1, -1), tactics);
                if(nm.x == -1 && nm.y == -1) MessageDialog("相手はパスです");
                else board.setStoneAndReverse(nm.x, nm.y, player[turn-1].getColor());
                changeTurn();
                board.evaluateBoard(); //ターン後の評価
                // board.printEval();
                repaint();
                addMouseListener(new MouseProc()); //マウスイベント処理を再開する
                //ゲーム終了確認
                if(board.num_grid_black == 0 && board.num_grid_white == 0) EndMessageDialog();
            } catch(InterruptedException ie){
                ie.printStackTrace();
            }
        }
    }
}
