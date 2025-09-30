package 콘솔기반프로그램;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

// 인터페이스: 예매 및 취소 기능 표준화
interface Reservable {
    boolean reserve(String customerName, int row, int col);
    boolean cancel(String customerName, int row, int col);
}

// 추상 클래스: 영화의 기본 속성과 메서드 정의
abstract class AbstractMovie {
    protected String title;
    protected String genre;
    protected int duration; // 분 단위
    protected double basePrice;

    public AbstractMovie(String title, String genre, int duration, double basePrice) {
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.basePrice = basePrice;
    }

    // 추상 메서드: 영화 유형별 가격 계산
    public abstract double calculatePrice();

    // 일반 메서드
    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "Movie: " + title + " (" + genre + ", " + duration + "분)";
    }
}

// 상속: 2D 영화
class Movie2D extends AbstractMovie {
    public Movie2D(String title, String genre, int duration, double basePrice) {
        super(title, genre, duration, basePrice);
    }

    @Override
    public double calculatePrice() {
        return basePrice; // 2D 영화는 기본 가격
    }
}

// 상속: 3D 영화
class Movie3D extends AbstractMovie {
    private static final double SURCHARGE = 3000; // 3D 추가 요금

    public Movie3D(String title, String genre, int duration, double basePrice) {
        super(title, genre, duration, basePrice);
    }

    @Override
    public double calculatePrice() {
        return basePrice + SURCHARGE; // 3D 영화는 추가 요금
    }
}

// 상영 클래스: 좌석 관리, 예매/취소 기능
class Screening implements Reservable {
    private AbstractMovie movie;
    private String time;
    private int screenNumber;
    private Seat[][] seats; // 2D 배열로 좌석 관리
    private ArrayList<Ticket> tickets; // 예매된 티켓 목록
    private static final int MAX_ROWS = 5; // 정적 멤버: 최대 행 수
    private static final int MAX_COLS = 5; // 정적 멤버: 최대 열 수

    // 정적 중첩 클래스: 상영관 설정
    public static class TheaterConfig {
        public static String getTheaterName() {
            return "LKH 시네마"; // 정적 메서드
        }
    }

    // 내부 클래스: 좌석 정보
    public class Seat {
        private boolean isReserved;
        private String customerName;

        public Seat() {
            this.isReserved = false;
            this.customerName = null;
        }

        public boolean reserve(String customerName) {
            if (!isReserved) {
                this.isReserved = true;
                this.customerName = customerName;
                return true;
            }
            return false;
        }

        public boolean cancel(String customerName) {
            if (isReserved && this.customerName.equals(customerName)) {
                this.isReserved = false;
                this.customerName = null;
                return true;
            }
            return false;
        }

        public boolean isReserved() {
            return isReserved;
        }

        public String getCustomerName() {
            return customerName;
        }
    }

    public Screening(AbstractMovie movie, String time, int screenNumber) {
        this.movie = movie;
        this.time = time;
        this.screenNumber = screenNumber;
        this.seats = new Seat[MAX_ROWS][MAX_COLS];
        this.tickets = new ArrayList<>();
        for (int i = 0; i < MAX_ROWS; i++) {
            for (int j = 0; j < MAX_COLS; j++) {
                seats[i][j] = new Seat();
            }
        }
    }

    @Override
    public boolean reserve(String customerName, int row, int col) {
        if (row >= 0 && row < MAX_ROWS && col >= 0 && col < MAX_COLS) {
            if (seats[row][col].reserve(customerName)) {
                tickets.add(new Ticket(this, row, col, customerName));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean cancel(String customerName, int row, int col) {
        if (row >= 0 && row < MAX_ROWS && col >= 0 && col < MAX_COLS) {
            if (seats[row][col].cancel(customerName)) {
                tickets.removeIf(ticket -> ticket.getRow() == row && ticket.getCol() == col);
                return true;
            }
        }
        return false;
    }

    // 콘솔용 좌석 상태 출력 (GUI에서는 사용되지 않음)
    public void displaySeats() {
        System.out.println("좌석 상태 (O: 빈 좌석, X: 예약됨):");
        for (int i = 0; i < MAX_ROWS; i++) {
            for (int j = 0; j < MAX_COLS; j++) {
                System.out.print(seats[i][j].isReserved() ? "X " : "O ");
            }
            System.out.println();
        }
    }

    public void displaySeats(String message) {
        System.out.println(message);
        displaySeats();
    }

    public AbstractMovie getMovie() {
        return movie;
    }

    public String getTime() {
        return time;
    }

    public Ticket findTicket(String customerName, int row, int col) {
        for (Ticket ticket : tickets) {
            if (ticket.getRow() == row && ticket.getCol() == col && ticket.getCustomerName().equals(customerName)) {
                return ticket;
            }
        }
        return null;
    }

    // GUI에서 좌석 배열 접근을 위한 getter
    public Seat[][] getSeats() {
        return seats;
    }
}

// 티켓 클래스
class Ticket {
    private Screening screening;
    private int row;
    private int col;
    private String customerName;
    private double price;

    public Ticket(Screening screening, int row, int col, String customerName) {
        this.screening = screening;
        this.row = row;
        this.col = col;
        this.customerName = customerName;
        this.price = screening.getMovie().calculatePrice();
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public String getCustomerName() {
        return customerName;
    }

    @Override
    public String toString() {
        return "티켓: " + screening.getMovie().getTitle() + " | 시간: " + screening.getTime() +
               " | 좌석: (" + (row + 1) + ", " + (col + 1) + ") | 고객: " + customerName +
               " | 가격: " + price + "원";
    }
}

// GUI 메인 클래스
public class CinemaSystemGUI {
    private static ArrayList<Screening> screenings = new ArrayList<>();
    private static int ticketCounter = 0;
    private JFrame frame;
    private JPanel seatPanel;
    private JLabel statusLabel;

    public CinemaSystemGUI() {
        // 영화 및 상영 초기화
        AbstractMovie movie2D = new Movie2D("썬더볼츠", "SF", 148, 15000);
        AbstractMovie movie3D = new Movie3D("미션 임파서블", "SF", 162, 15000);
        screenings.add(new Screening(movie2D, "2025-05-11 14:00", 1));
        screenings.add(new Screening(movie3D, "2025-05-11 17:00", 2));

        // 메인 프레임 설정
        frame = new JFrame(Screening.TheaterConfig.getTheaterName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // 버튼 패널
        JPanel buttonPanel = new JPanel(new GridLayout(7, 1, 10, 10));
        JButton showScreeningsBtn = new JButton("1. 상영 목록 보기");
        JButton showSeatsBtn = new JButton("2. 좌석 상태 보기");
        JButton reserveBtn = new JButton("3. 예매하기");
        JButton cancelBtn = new JButton("4. 예매 취소하기");
        JButton viewTicketBtn = new JButton("5. 티켓 정보 보기");
        JButton ticketCountBtn = new JButton("6. 총 발행 티켓 수 확인");
        JButton exitBtn = new JButton("7. 종료");

        // 버튼 이벤트 리스너
        showScreeningsBtn.addActionListener(e -> showScreenings());
        showSeatsBtn.addActionListener(e -> showSeats());
        reserveBtn.addActionListener(e -> reserveTicket());
        cancelBtn.addActionListener(e -> cancelTicket());
        viewTicketBtn.addActionListener(e -> viewTicket());
        ticketCountBtn.addActionListener(e -> showTicketCount());
        exitBtn.addActionListener(e -> System.exit(0));

        buttonPanel.add(showScreeningsBtn);
        buttonPanel.add(showSeatsBtn);
        buttonPanel.add(reserveBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(viewTicketBtn);
        buttonPanel.add(ticketCountBtn);
        buttonPanel.add(exitBtn);

        // 상태 레이블
        statusLabel = new JLabel("환영합니다! " + Screening.TheaterConfig.getTheaterName(), SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 16)); // 맑은 고딕 대신 SansSerif로 대체

        // 좌석 패널
        seatPanel = new JPanel();
        seatPanel.setLayout(new GridLayout(5, 5, 5, 5));

        // 프레임 구성
        frame.add(statusLabel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.WEST);
        frame.add(seatPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void showScreenings() {
        StringBuilder sb = new StringBuilder("<html><h3>상영 목록:</h3><ul>");
        for (int i = 0; i < screenings.size(); i++) {
            Screening s = screenings.get(i);
            sb.append("<li>").append(i + 1).append(". ").append(s.getMovie().toString())
              .append(" | 시간: ").append(s.getTime()).append("</li>");
        }
        sb.append("</ul></html>");
        statusLabel.setText(sb.toString());
        seatPanel.removeAll();
        seatPanel.revalidate();
        seatPanel.repaint();
    }

    private void showSeats() {
        String screenNumStr = JOptionPane.showInputDialog(frame, "상영 번호를 선택하세요 (1-" + screenings.size() + "):");
        try {
            int screenNum = Integer.parseInt(screenNumStr) - 1;
            if (screenNum >= 0 && screenNum < screenings.size()) {
                Screening screening = screenings.get(screenNum);
                seatPanel.removeAll();
                seatPanel.setLayout(new GridLayout(5, 5, 5, 5));
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 5; j++) {
                        JButton seatBtn = new JButton(screening.getSeats()[i][j].isReserved() ? "X" : "O");
                        seatBtn.setEnabled(false);
                        seatBtn.setBackground(screening.getSeats()[i][j].isReserved() ? Color.RED : Color.GREEN);
                        seatPanel.add(seatBtn);
                    }
                }
                statusLabel.setText("상영 " + (screenNum + 1) + " 좌석 상태 (O: 빈 좌석, X: 예약됨)");
                seatPanel.revalidate();
                seatPanel.repaint();
            } else {
                JOptionPane.showMessageDialog(frame, "잘못된 상영 번호입니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException | NullPointerException e) {
            JOptionPane.showMessageDialog(frame, "잘못된 입력입니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reserveTicket() {
        String screenNumStr = JOptionPane.showInputDialog(frame, "상영 번호를 선택하세요 (1-" + screenings.size() + "):");
        try {
            int screenNum = Integer.parseInt(screenNumStr) - 1;
            if (screenNum >= 0 && screenNum < screenings.size()) {
                Screening screening = screenings.get(screenNum);
                String customerName = JOptionPane.showInputDialog(frame, "고객 이름을 입력하세요:");
                if (customerName == null || customerName.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "고객 이름을 입력해야 합니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String rowStr = JOptionPane.showInputDialog(frame, "행 번호를 입력하세요 (1-5):");
                String colStr = JOptionPane.showInputDialog(frame, "열 번호를 입력하세요 (1-5):");
                int row = Integer.parseInt(rowStr) - 1;
                int col = Integer.parseInt(colStr) - 1;
                if (screening.reserve(customerName, row, col)) {
                    ticketCounter++;
                    Ticket ticket = screening.findTicket(customerName, row, col);
                    JOptionPane.showMessageDialog(frame, "예매 성공!\n" + ticket, "성공", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "예매 실패: 이미 예약된 좌석이거나 잘못된 좌석 번호입니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "잘못된 상영 번호입니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException | NullPointerException e) {
            JOptionPane.showMessageDialog(frame, "잘못된 입력입니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelTicket() {
        String screenNumStr = JOptionPane.showInputDialog(frame, "상영 번호를 선택하세요 (1-" + screenings.size() + "):");
        try {
            int screenNum = Integer.parseInt(screenNumStr) - 1;
            if (screenNum >= 0 && screenNum < screenings.size()) {
                Screening screening = screenings.get(screenNum);
                String customerName = JOptionPane.showInputDialog(frame, "고객 이름을 입력하세요:");
                if (customerName == null || customerName.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "고객 이름을 입력해야 합니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String rowStr = JOptionPane.showInputDialog(frame, "행 번호를 입력하세요 (1-5):");
                String colStr = JOptionPane.showInputDialog(frame, "열 번호를 입력하세요 (1-5):");
                int row = Integer.parseInt(rowStr) - 1;
                int col = Integer.parseInt(colStr) - 1;
                if (screening.cancel(customerName, row, col)) {
                    JOptionPane.showMessageDialog(frame, "취소 성공!", "성공", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "취소 실패: 예약되지 않은 좌석이거나 잘못된 정보입니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "잘못된 상영 번호입니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException | NullPointerException e) {
            JOptionPane.showMessageDialog(frame, "잘못된 입력입니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewTicket() {
        String screenNumStr = JOptionPane.showInputDialog(frame, "상영 번호를 선택하세요 (1-" + screenings.size() + "):");
        try {
            int screenNum = Integer.parseInt(screenNumStr) - 1;
            if (screenNum >= 0 && screenNum < screenings.size()) {
                Screening screening = screenings.get(screenNum);
                String customerName = JOptionPane.showInputDialog(frame, "고객 이름을 입력하세요:");
                if (customerName == null || customerName.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "고객 이름을 입력해야 합니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String rowStr = JOptionPane.showInputDialog(frame, "행 번호를 입력하세요 (1-5):");
                String colStr = JOptionPane.showInputDialog(frame, "열 번호를 입력하세요 (1-5):");
                int row = Integer.parseInt(rowStr) - 1;
                int col = Integer.parseInt(colStr) - 1;
                Ticket ticket = screening.findTicket(customerName, row, col);
                if (ticket != null) {
                    JOptionPane.showMessageDialog(frame, ticket.toString(), "티켓 정보", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "티켓 정보 조회 실패: 예약되지 않은 좌석이거나 잘못된 정보입니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "잘못된 상영 번호입니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException | NullPointerException e) {
            JOptionPane.showMessageDialog(frame, "잘못된 입력입니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTicketCount() {
        JOptionPane.showMessageDialog(frame, "총 발행 티켓 수: " + ticketCounter, "티켓 수", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CinemaSystemGUI());
    }
}