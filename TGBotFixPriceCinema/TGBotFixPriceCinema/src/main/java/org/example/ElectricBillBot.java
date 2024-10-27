package org.example;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class ElectricBillBot extends TelegramLongPollingBot {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/....";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "....";
    private boolean isValidDate(String date) {
        String regex = "^\\d{4}-\\d{2}-\\d{2}$";
        return date.matches(regex);
    }
    @Override
    public String getBotUsername() {
        return "....";
    }
    private Map<Long, MovieData> moviesTemp = new HashMap<>();
    private Map<Long, Integer> selectedGenre = new HashMap<>();
    @Override
    public String getBotToken() {
        return "....";
    }



    private Map<Long, Integer> selectedRating = new HashMap<>();
    private Map<Long, UserState> userStates = new HashMap<>();
    private Map<Long, Map<String, String>> registrationData = new HashMap<>();
    private Map<Long, Map<String, String>> addingShowtimeData = new HashMap<>();
    private Map<Long, String> selectedMovie = new HashMap<>();
    private enum UserState {
        NOT_LOGGED_IN,
        REGISTERING_EMAIL,
        REGISTERING_FIRST_NAME,
        REGISTERING_LAST_NAME,
        REGISTERING_PASSWORD,
        LOGGED_IN,
        LOGIN_EMAIL,
        LOGIN_PASSWORD,
        ADMIN_PANEL,
        ADDING_SHOWTIME,
        DELETING_SHOWTIME,
        TOP_UP_BALANCE,
        VIEWING_TICKETS,
        ADDING_MOVIE,
        RATING_FOR_REVIEW,
        COMMENT_FOR_REVIEW,
        ADDING_MOVIE_TITLE,
        ADDING_MOVIE_DESCRIPTION,
        ADDING_MOVIE_RELEASE_DATE,
        ADDING_MOVIE_DURATION,
        SELECTING_MOVIE_FOR_SHOWTIME,
        ADDING_SHOWTIME_DATE,
        BUYING_TICKET,
        ADDING_SHOWTIME_TIME,
        ADDING_SHOWTIME_SEATS,
        SELECTING_MOVIE_FOR_REVIEW,
        SELECTING_GENRE_FOR_MOVIE
    }
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            UserState state = userStates.getOrDefault(chatId, UserState.NOT_LOGGED_IN);
            if (messageText.equals("Назад")) {
                if (
                        state == UserState.REGISTERING_EMAIL ||
                                state == UserState.REGISTERING_FIRST_NAME ||
                                state == UserState.REGISTERING_LAST_NAME ||
                                state == UserState.REGISTERING_PASSWORD ||
                                state == UserState.LOGIN_EMAIL ||
                                state == UserState.LOGIN_PASSWORD) {

                    // Выполняется, если state один из перечисленных
                    userStates.put(chatId, UserState.NOT_LOGGED_IN);

                } else if ( state == UserState.ADDING_SHOWTIME ||
                        state == UserState.DELETING_SHOWTIME ||
                        state == UserState.ADDING_MOVIE ||
                        state == UserState.ADDING_MOVIE_DESCRIPTION ||
                        state == UserState.ADDING_MOVIE_DURATION ||
                        state == UserState.ADDING_MOVIE_TITLE ||
                        state == UserState.ADDING_MOVIE_RELEASE_DATE||
                        state == UserState.SELECTING_MOVIE_FOR_SHOWTIME||

                        state == UserState.ADDING_SHOWTIME_TIME ||
                        state == UserState.SELECTING_GENRE_FOR_MOVIE ||
                        state == UserState.ADDING_SHOWTIME_SEATS ||
                        state == UserState.ADMIN_PANEL
                ) {

                    userStates.put(chatId, UserState.ADMIN_PANEL);
                    showAdminPanel(chatId);

                }
                else if (( state == UserState.NOT_LOGGED_IN )) {
                    sendNotLoggedInMenu(chatId);
                }
                else {
                    // Выполняется, если state один из перечисленных
                    userStates.put(chatId, UserState.LOGGED_IN);
                    sendLoggedInMenu(chatId);
                }
            }
            System.out.println("STATE = " + state);
            if(!Objects.equals(messageText, "Назад")){
                switch (state) {
                    case NOT_LOGGED_IN:
                        handleNotLoggedInState(chatId, messageText);
                        break;
                    case LOGGED_IN:
                        handleLoggedInState(chatId, messageText);
                        break;
                    case ADMIN_PANEL:
                        handleAdminPanelActions(chatId, messageText);
                        break;
                    case REGISTERING_EMAIL:
                        handleRegistrationState(chatId, messageText, state);
                        break;
                    case REGISTERING_FIRST_NAME:
                        handleRegistrationState(chatId, messageText, state);
                        break;
                    case REGISTERING_LAST_NAME:
                        handleRegistrationState(chatId, messageText, state);
                        break;
                    case REGISTERING_PASSWORD:
                        handleRegistrationState(chatId, messageText, state);
                        break;
                    case LOGIN_EMAIL:
                    case LOGIN_PASSWORD:
                        handleLoginState(chatId, messageText, state);
                        break;
                    case ADDING_SHOWTIME:
                        addShowtime(chatId);
                        break;
                    case DELETING_SHOWTIME:
                        handleDeleteShowtime(chatId, messageText);
                        break;
                    case ADDING_MOVIE:
                        addMovie(chatId);
                        break;
                    case BUYING_TICKET:
                        handleBuyTicket(chatId, messageText);
                        break;
                    case ADDING_MOVIE_TITLE:
                        handleAddMovieTitle(chatId, messageText);
                        break;
                    case ADDING_MOVIE_RELEASE_DATE:
                        handleAddMovieReleaseDate(chatId, messageText);
                        break;
                    case ADDING_MOVIE_DURATION:
                        handleAddMovieDuration(chatId, messageText);
                        break;
                    case SELECTING_MOVIE_FOR_SHOWTIME:
                        handleSelectMovieForShowtime(chatId, messageText);
                        break;
                    case ADDING_SHOWTIME_DATE:
                        handleAddShowtimeDate(chatId, messageText);
                        break;
                    case ADDING_SHOWTIME_TIME:
                        handleAddShowtimeTime(chatId, messageText);
                        break;
                    case ADDING_SHOWTIME_SEATS:
                        handleAddShowtimeSeats(chatId, messageText);
                        break;
                    case SELECTING_GENRE_FOR_MOVIE:
                        handleSelectGenreForMovie(chatId, messageText);
                        break;
                    case TOP_UP_BALANCE:
                        handleTopUpBalanceAmount(chatId, messageText);
                        break;
                    case SELECTING_MOVIE_FOR_REVIEW: // Добавляем случай для выбора фильма
                        handleSelectMovieForReview(chatId, messageText);
                        break;
                    case RATING_FOR_REVIEW: // Добавляем случай для оценки
                        handleRatingForReview(chatId, messageText);
                        break;
                    case COMMENT_FOR_REVIEW: // Добавляем случай для комментария
                        handleCommentForReview(chatId, messageText);
                        break;
                    default:
                        sendMessage(chatId, "Неизвестное состояние. Пожалуйста, попробуйте снова.");
                        break;
                }
            }
        }
    }


    private void showMovieRatings(long chatId) {
        List<MovieRatingInfo> movieRatings = getMovieRatings();
        if (movieRatings.isEmpty()) {
            sendMessage(chatId, "В базе данных нет отзывов о фильмах.");
            return;
        }

        StringBuilder messageText = new StringBuilder("Рейтинг фильмов:\n\n");
        for (MovieRatingInfo ratingInfo : movieRatings) {
            messageText.append(ratingInfo.title).append(" - ").append(ratingInfo.averageRating).append(" (").append(ratingInfo.ratingCount).append(" оценок)\n");
        }

        sendMessage(chatId, messageText.toString());
    }

    private List<MovieRatingInfo> getMovieRatings() {
        List<MovieRatingInfo> movieRatings = new ArrayList<>();
        String sql = "SELECT m.title, AVG(r.rating) as average_rating, COUNT(r.review_id) as rating_count " +
                "FROM movies m " +
                "LEFT JOIN reviews r ON m.movie_id = r.movie_id " +
                "GROUP BY m.movie_id, m.title " +
                "ORDER BY m.title";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                double averageRating = resultSet.getDouble("average_rating");
                int ratingCount = resultSet.getInt("rating_count");
                MovieRatingInfo ratingInfo = new MovieRatingInfo(title, averageRating, ratingCount);
                movieRatings.add(ratingInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movieRatings;
    }



    private void handleLoggedInState(long chatId, String messageText) {
        UserState state = userStates.get(chatId);
        switch (state) {
            case LOGGED_IN:
                switch (messageText) {
                    case "Купить билет":
                        buyTicket(chatId); // Вызываем функцию покупки билета
                        break;

                    case "Рейтинг фильмов":
                        showMovieRatings(chatId); // Вызов новой функции
                        break;

                    case "Оставить отзыв":
                        leaveReview(chatId); // Вызываем функцию оставления отзыва
                        break;
                    case "Выйти":
                        userStates.put(chatId, UserState.NOT_LOGGED_IN);
                        sendMessage(chatId, "Вы вышли из аккаунта");
                        sendNotLoggedInMenu(chatId);
                        break;
                    case "Пополнить баланс":
                        userStates.put(chatId, UserState.TOP_UP_BALANCE);
                        sendMessage(chatId, "На сколько хотите пополнить баланс?", createBackKeyboardMarkup());
                        break;

                    case "Панель администратора":
                        showAdminPanel(chatId);
                        userStates.put(chatId, UserState.ADMIN_PANEL);
                        break;
                    case "Мои билеты":
                        // Вот здесь мы обрабатываем кнопку "Мои билеты"
                        userStates.put(chatId, UserState.VIEWING_TICKETS);
                        viewTickets(chatId);
                        break;
                    default:
                        sendLoggedInMenu(chatId);
                }
                break;
            case ADMIN_PANEL:
                handleAdminPanelActions(chatId, messageText);
                break;
            case ADDING_SHOWTIME:
                addShowtime(chatId);
                break;
            case DELETING_SHOWTIME:
                deleteShowtime(chatId);
                break;
            case ADDING_MOVIE:
                addMovie(chatId);
                break;
            case VIEWING_TICKETS: // Обрабатываем состояние просмотра билетов
                handleViewTickets(chatId, messageText);
                break; // Не забываем добавить break для окончания case
            // ... (другие case)
        }
    }

    private void handleViewTickets(long chatId, String messageText) {
        int userId = getUserIdByChatId(chatId);
        if (userId == -1) {
            sendMessage(chatId, "Ошибка: не удалось найти пользователя.");
            userStates.put(chatId, UserState.LOGGED_IN);
            return;
        }

        List<TicketInfo> tickets = getUserTickets(userId);
        if (tickets.isEmpty()) {
            sendMessage(chatId, "У вас нет купленных билетов.");
            userStates.put(chatId, UserState.LOGGED_IN);
            sendLoggedInMenu(chatId);
            return;
        }

        StringBuilder messageTextBuilder = new StringBuilder("Ваши билеты:\n\n");
        for (TicketInfo ticket : tickets) {
            messageTextBuilder.append(ticket.toString()).append("\n");
        }

        sendMessage(chatId, messageTextBuilder.toString());
        userStates.put(chatId, UserState.LOGGED_IN);
        sendLoggedInMenu(chatId);
    }
    private void handleAdminPanelActions(long chatId, String messageText) {
        switch (messageText) {
            case "Добавить новый сеанс":
                addShowtime(chatId);
                break;
            case "Удалить сеанс":
                deleteShowtime(chatId);
                break;
            case "Добавить фильм":
                addMovie(chatId);
                break;
            case "Выйти": // Обработка кнопки "Выйти"
                userStates.put(chatId, UserState.NOT_LOGGED_IN);
                sendNotLoggedInMenu(chatId);
                break;
            default:
                sendMessage(chatId, "Неверный выбор. Вернуться к меню администратора.");
                showAdminPanel(chatId);
        }
    }
    private void showAdminPanel(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Вы в панели администратора. Выберите действие:");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Добавить новый сеанс");
        KeyboardRow row2 = new KeyboardRow();
        row2.add("Удалить сеанс");
        row2.add("Добавить фильм");
        KeyboardRow row3 = new KeyboardRow(); // Добавляем новую строку для "Назад"
        row3.add("Выйти");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3); // Добавляем строку в клавиатуру
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void addShowtime(long chatId) {
        List<String> movies = getMoviesList();
        if (movies.isEmpty()) {
            sendMessage(chatId, "В базе данных нет фильмов. Добавьте фильмы, прежде чем создавать сеансы.");
            userStates.put(chatId, UserState.ADMIN_PANEL);
            return;
        }
        sendMessage(chatId, "Выберите фильм для сеанса:", createKeyboardMarkup(movies, true)); // Добавьте "Отмена"
        userStates.put(chatId, UserState.SELECTING_MOVIE_FOR_SHOWTIME);
    }

    private ReplyKeyboardMarkup createKeyboardMarkup(List<String> movies, boolean withBack) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        for (String movie : movies) {
            row.add(movie);
        }
        keyboard.add(row);

        if (withBack) { // Добавляем "Назад", если withBack = true
            KeyboardRow backRow = new KeyboardRow();
            backRow.add("Назад");
            keyboard.add(backRow);
        }

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }


    private ReplyKeyboardMarkup createKeyboardMarkup(List<String> movies) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        for (String movie : movies) {
            row.add(movie);
        }
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }
    private void handleSelectMovieForShowtime(long chatId, String messageText) {
        selectedMovie.put(chatId, messageText);
        sendMessage(chatId, "Введите дату сеанса (YYYY-MM-DD):", createBackKeyboardMarkup());

        userStates.put(chatId, UserState.ADDING_SHOWTIME_DATE);
    }
    private void handleAddShowtimeDate(long chatId, String date) {
        Map<String, String> showtimeData = addingShowtimeData.getOrDefault(chatId, new HashMap<>());
        if (isValidDate(date)) {
            // Получаем текущую дату и время
            LocalDateTime now = LocalDateTime.now();

            // Преобразуем введенную дату в объект LocalDateTime
            LocalDateTime enteredDate = LocalDateTime.parse(date + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            // Проверяем, не раньше ли введенная дата, чем текущая
            if (enteredDate.isBefore(now)) {
                sendMessage(chatId, "Дата сеанса не может быть раньше текущей даты.");
                sendMessage(chatId, "Введите дату сеанса (YYYY-MM-DD):", createBackKeyboardMarkup());
                userStates.put(chatId, UserState.ADDING_SHOWTIME_DATE);
                return;
            }

            showtimeData.put("date", date);
            addingShowtimeData.put(chatId, showtimeData);
            sendMessage(chatId, "Введите время сеанса (HH:mm):", createBackKeyboardMarkup());

            userStates.put(chatId, UserState.ADDING_SHOWTIME_TIME);
        } else {
            sendMessage(chatId, "Некорректный формат даты. Пожалуйста, введите дату в формате YYYY-MM-DD (например, 2024-10-20).");
            sendMessage(chatId, "Введите дату сеанса (YYYY-MM-DD):", createBackKeyboardMarkup());

            userStates.put(chatId, UserState.ADDING_SHOWTIME_DATE);
        }
    }
    private void handleAddShowtimeTime(long chatId, String time) {
        Map<String, String> showtimeData = addingShowtimeData.getOrDefault(chatId, new HashMap<>());
        if (isValidTime(time)) {
            showtimeData.put("time", time);
            addingShowtimeData.put(chatId, showtimeData);
            sendMessage(chatId, "Введите количество мест:", createBackKeyboardMarkup());

            userStates.put(chatId, UserState.ADDING_SHOWTIME_SEATS);
        } else {
            sendMessage(chatId, "Некорректный формат времени. Пожалуйста, введите время в формате HH:mm (например, 18:15).");
            sendMessage(chatId, "Введите время сеанса (HH:mm):", createBackKeyboardMarkup());

            userStates.put(chatId, UserState.ADDING_SHOWTIME_TIME);
        }
    }
    private void handleAddShowtimeSeats(long chatId, String seats) {
        Map<String, String> showtimeData = addingShowtimeData.getOrDefault(chatId, new HashMap<>());
        try {
            int seatsInt = Integer.parseInt(seats);
            showtimeData.put("seats", seats);
            addingShowtimeData.put(chatId, showtimeData);
            boolean success = saveShowtimeToDatabase(chatId, showtimeData);
            if (success) {
                sendMessage(chatId, "Сеанс успешно добавлен!");
            } else {
                sendMessage(chatId, "Произошла ошибка при добавлении сеанса.");
            }
            userStates.put(chatId, UserState.ADMIN_PANEL);
            showAdminPanel(chatId);
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Пожалуйста, введите корректное количество мест (целое число).");
            userStates.put(chatId, UserState.ADDING_SHOWTIME_SEATS);
        }
    }
    private static final double TICKET_PRICE = 200.00;
    private boolean saveShowtimeToDatabase(long chatId, Map<String, String> showtimeData) {
        String selectedMovieTitle = selectedMovie.get(chatId);
        int movieId = getMovieIdByTitle(selectedMovieTitle);
        String date = showtimeData.get("date");
        String time = showtimeData.get("time");
        int seats = Integer.parseInt(showtimeData.get("seats"));
        Timestamp showtimeTimestamp = Timestamp.valueOf(date + " " + time + ":00");
        String sql = "INSERT INTO showtimes (movie_id, showtime, available_seats) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, movieId);
            preparedStatement.setTimestamp(2, showtimeTimestamp);
            preparedStatement.setInt(3, seats);
            int affectedRows = preparedStatement.executeUpdate();
            selectedMovie.remove(chatId);
            addingShowtimeData.remove(chatId);
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    private int getMovieIdByTitle(String title) {
        String sql = "SELECT movie_id FROM movies WHERE title = ?";
        int movieId = -1;
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, title);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                movieId = resultSet.getInt("movie_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movieId;
    }
    private void deleteShowtime(long chatId) {
        List<ShowtimeInfo> showtimes = getShowtimesList();
        if (showtimes.isEmpty()) {
            sendMessage(chatId, "В настоящее время нет доступных сеансов для удаления.");
            userStates.put(chatId, UserState.ADMIN_PANEL);
            showAdminPanel(chatId);
            return;
        }
        sendMessage(chatId, "Выберите сеанс для удаления:", createShowtimesKeyboard(showtimes));
        userStates.put(chatId, UserState.DELETING_SHOWTIME);
    }
    private List<ShowtimeInfo> getShowtimesList() {
        List<ShowtimeInfo> showtimes = new ArrayList<>();
        String sql = "SELECT s.showtime_id, m.title, s.showtime, s.available_seats " +
                "FROM showtimes s " +
                "JOIN movies m ON s.movie_id = m.movie_id " +
                "ORDER BY s.showtime";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int showtimeId = resultSet.getInt("showtime_id");
                String movieTitle = resultSet.getString("title");
                Timestamp showtime = resultSet.getTimestamp("showtime");
                int availableSeats = resultSet.getInt("available_seats");
                ShowtimeInfo info = new ShowtimeInfo(showtimeId, movieTitle, showtime, availableSeats);
                showtimes.add(info);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return showtimes;
    }
    private ReplyKeyboardMarkup createShowtimesKeyboard(List<ShowtimeInfo> showtimes) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        for (ShowtimeInfo showtime : showtimes) {
            KeyboardRow row = new KeyboardRow();
            row.add(showtime.toString());
            keyboard.add(row);
        }
        KeyboardRow cancelRow = new KeyboardRow();
        cancelRow.add("Отмена");
        keyboard.add(cancelRow);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        return keyboardMarkup;
    }
    private void handleDeleteShowtime(long chatId, String messageText) {
        if (messageText.equals("Отмена")) {
            sendMessage(chatId, "Удаление сеанса отменено.");
            userStates.put(chatId, UserState.ADMIN_PANEL);
            showAdminPanel(chatId);
            return;
        }
        int showtimeId = extractShowtimeId(messageText);
        if (showtimeId == -1) {
            sendMessage(chatId, "Неверный формат выбора сеанса. Попробуйте еще раз.");
            return;
        }
        System.out.println("Попытка удаления сеанса с ID: " + showtimeId);
        if (deleteShowtimeFromDatabase(showtimeId)) {
            sendMessage(chatId, "Сеанс успешно удален.");
            System.out.println("Сеанс успешно удален из базы данных.");
        } else {
            sendMessage(chatId, "Произошла ошибка при удалении сеанса.");
            System.out.println("Ошибка при удалении сеанса из базы данных.");
        }
        userStates.put(chatId, UserState.ADMIN_PANEL);
        showAdminPanel(chatId);
    }
    private int extractShowtimeId(String messageText) {
        try {
            String[] parts = messageText.split(" - ");
            int id = Integer.parseInt(parts[0]);
            System.out.println("Извлеченный ID сеанса: " + id);
            return id;
        } catch (Exception e) {
            System.out.println("Ошибка при извлечении ID сеанса: " + e.getMessage());
            return -1;
        }
    }
    private boolean deleteShowtimeFromDatabase(int showtimeId) {
        String deleteTicketsSql = "DELETE FROM tickets WHERE showtime_id = ?";
        String deleteTransactionsSql = "DELETE FROM transactions WHERE showtime_id = ?";
        String deleteShowtimeSql = "DELETE FROM showtimes WHERE showtime_id = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // Start a transaction
            connection.setAutoCommit(false);

            // Delete related transactions first
            try (PreparedStatement transactionsStatement = connection.prepareStatement(deleteTransactionsSql)) {
                transactionsStatement.setInt(1, showtimeId);
                transactionsStatement.executeUpdate();
            }
            try (PreparedStatement ticketsStatement = connection.prepareStatement(deleteTicketsSql)) {
                ticketsStatement.setInt(1, showtimeId);
                ticketsStatement.executeUpdate();
            }
            // Delete the showtime
            try (PreparedStatement showtimeStatement = connection.prepareStatement(deleteShowtimeSql)) {
                showtimeStatement.setInt(1, showtimeId);
                int affectedRows = showtimeStatement.executeUpdate();
                connection.commit(); // Commit the transaction if both deletions are successful
                System.out.println("Попытка удаления сеанса с ID: " + showtimeId);
                System.out.println("Затронуто строк: " + affectedRows);
                return affectedRows > 0;
            } catch (SQLException e) {
                connection.rollback(); // Rollback if there’s an error with deleting the showtime
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при удалении сеанса: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    private void addMovie(long chatId) {
        List<String> genres = getGenresList();
        if (genres.isEmpty()) {
            sendMessage(chatId, "В базе данных нет жанров. Добавьте жанры, прежде чем добавлять фильмы.");
            userStates.put(chatId, UserState.ADMIN_PANEL);
            return;
        }
        userStates.put(chatId, UserState.SELECTING_GENRE_FOR_MOVIE);

        sendMessage(chatId, "Выберите жанр для фильма:", createKeyboardMarkup(genres, true)); // Добавьте "Назад"
    }

    private void handleSelectGenreForMovie(long chatId, String messageText) {
        int genreId = getGenreIdByName(messageText);
        if(!Objects.equals(messageText, "Назад")){
            if (genreId == -1) {
                sendMessage(chatId, "Некорректный жанр. Пожалуйста, выберите жанр из списка.");
                userStates.put(chatId, UserState.ADDING_MOVIE);
                return;
            }
            selectedGenre.put(chatId, genreId);
            sendMessage(chatId, "Введите название фильма:", createBackKeyboardMarkup());

            userStates.put(chatId, UserState.ADDING_MOVIE_TITLE);
        }
    }
    private void handleAddMovieTitle(long chatId, String title) {
        moviesTemp.put(chatId, new MovieData(title));
        sendMessage(chatId, "Введите дату выхода фильма (YYYY-MM-DD):", createBackKeyboardMarkup());
        userStates.put(chatId, UserState.ADDING_MOVIE_RELEASE_DATE);
    }

    private void handleAddMovieReleaseDate(long chatId, String releaseDate) {
        MovieData movieData = moviesTemp.get(chatId);
        if (movieData != null) {
            if (isValidDate(releaseDate)) {
                if (isValidFullDate(releaseDate)) {
                    movieData.setReleaseDate(releaseDate);
                    sendMessage(chatId, "Введите длительность фильма в минутах:", createBackKeyboardMarkup());
                    userStates.put(chatId, UserState.ADDING_MOVIE_DURATION);
                } else {
                    sendMessage(chatId, "Некорректная дата. Проверьте, что месяц находится в диапазоне от 1 до 12, а день - от 1 до 31.");
                    sendMessage(chatId, "Введите дату выхода фильма (YYYY-MM-DD):", createBackKeyboardMarkup());
                    userStates.put(chatId, UserState.ADDING_MOVIE_RELEASE_DATE);
                }
            } else {
                sendMessage(chatId, "Некорректный формат даты. Пожалуйста, введите дату в формате YYYY-MM-DD.");
                sendMessage(chatId, "Введите дату выхода фильма (YYYY-MM-DD):", createBackKeyboardMarkup());
                userStates.put(chatId, UserState.ADDING_MOVIE_RELEASE_DATE);
            }
        } else {
            sendMessage(chatId, "Ошибка при добавлении фильма. Пожалуйста, попробуйте снова.");
            userStates.put(chatId, UserState.ADMIN_PANEL);
        }
    }
    private boolean isValidFullDate(String date) {
        if (!isValidDate(date)) {
            return false;
        }
        try {
            String[] parts = date.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            if (month < 1 || month > 12 || day < 1 || day > 31) {
                return false;
            }
            if (month == 2) {
                if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                    return day <= 29;
                } else {
                    return day <= 28;
                }
            } else if (month == 4 || month == 6 || month == 9 || month == 11) {
                return day <= 30;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private void handleAddMovieDuration(long chatId, String duration) {
        MovieData movieData = moviesTemp.get(chatId);
        if (movieData != null) {
            try {
                int durationInt = Integer.parseInt(duration);
                movieData.setDuration(durationInt);
                int genreId = selectedGenre.getOrDefault(chatId, -1);
                if (genreId == -1) {
                    sendMessage(chatId, "Ошибка: жанр не выбран. Пожалуйста, попробуйте снова.");
                    userStates.put(chatId, UserState.ADDING_MOVIE);
                    return;
                }
                saveMovieToDatabase(movieData, genreId);
                sendMessage(chatId, "Фильм успешно добавлен!");
                userStates.put(chatId, UserState.LOGGED_IN);
            } catch (NumberFormatException e) {
                sendMessage(chatId, "Пожалуйста, введите корректное значение для длительности.");
                return;
            }
        }
        userStates.put(chatId, UserState.ADMIN_PANEL);
    }
    private void saveMovieToDatabase(MovieData movieData, int genreId) {
        String sql = "INSERT INTO movies (title, description, release_date, duration, genre_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, movieData.getTitle());
            preparedStatement.setString(2, movieData.getDescription());
            preparedStatement.setDate(3, Date.valueOf(movieData.getReleaseDate()));
            preparedStatement.setInt(4, movieData.getDuration());
            preparedStatement.setInt(5, genreId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private List<String> getGenresList() {
        List<String> genres = new ArrayList<>();
        String sql = "SELECT name FROM genres";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                genres.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genres;
    }
    private int getGenreIdByName(String genreName) {
        String sql = "SELECT genre_id FROM genres WHERE name = ?";
        int genreId = -1;
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, genreName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                genreId = resultSet.getInt("genre_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genreId;
    }
    // ... (rest of the code)

    private void handleRegistrationState(long chatId, String messageText, UserState state) {
        Map<String, String> userData = registrationData.getOrDefault(chatId, new HashMap<>());

        if (messageText.equals("Назад")) {
            userStates.put(chatId, UserState.NOT_LOGGED_IN);
            sendNotLoggedInMenu(chatId);
            registrationData.remove(chatId); // Удаляем данные регистрации
            return;
        }

        switch (state) {
            case REGISTERING_EMAIL:
                userData.put("email", messageText);
                userStates.put(chatId, UserState.REGISTERING_FIRST_NAME);

                sendMessage(chatId, "Введите ваше имя:", createBackKeyboardMarkup());
                break;
            case REGISTERING_FIRST_NAME:
                userData.put("first_name", messageText);
                userStates.put(chatId, UserState.REGISTERING_LAST_NAME);

                sendMessage(chatId, "Введите вашу фамилию:", createBackKeyboardMarkup());
                break;
            case REGISTERING_LAST_NAME:
                userData.put("last_name", messageText);

                userStates.put(chatId, UserState.REGISTERING_PASSWORD);
                sendMessage(chatId, "Введите пароль:");
                break;
            case REGISTERING_PASSWORD:
                userData.put("password", messageText);
                registerUser(chatId, userData);
                break;
            default:
                sendMessage(chatId, "Неверный ввод. Пожалуйста, попробуйте снова.");
                handleRegistrationState(chatId, messageText, state);
        }

        registrationData.put(chatId, userData);
    }

    private ReplyKeyboardMarkup createBackKeyboardMarkup() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Назад");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }
    // ... (rest of the code)
    private void registerUser(long chatId, Map<String, String> userData) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false);
            String insertUserSQL = "INSERT INTO users (first_name, last_name, email, password_hash, chat_id) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertUserSQL, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, userData.get("first_name"));
            pstmt.setString(2, userData.get("last_name"));
            pstmt.setString(3, userData.get("email"));
            pstmt.setString(4, userData.get("password"));
            pstmt.setLong(5, chatId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Ошибка: создание пользователя не завершилось.");
            }
            generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int userId = generatedKeys.getInt(1);
                conn.commit();
                sendMessage(chatId, "Регистрация завершена! Теперь можете войти в аккаунт!");
                System.out.println("Регистрация завершена! Теперь можете войти в аккаунт!");
                userStates.put(chatId, UserState.NOT_LOGGED_IN);
                sendNotLoggedInMenu(chatId);
            } else {
                throw new SQLException("Ошибка: не удалось получить ID пользователя.");
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.out.println("Ошибка отката: " + rollbackEx.getMessage());
                }
            }
            sendMessage(chatId, "Произошла ошибка при регистрации: " + e.getMessage());
            userStates.put(chatId, UserState.NOT_LOGGED_IN);
            sendNotLoggedInMenu(chatId);
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Ошибка закрытия ресурсов: " + e.getMessage());
            }
            registrationData.remove(chatId);
        }
    }
    private void startRegistration(long chatId) {
        userStates.put(chatId, UserState.REGISTERING_EMAIL);
        registrationData.put(chatId, new HashMap<>());

        sendMessage(chatId, "Введите ваш email:", createBackKeyboardMarkup()); // Добавьте "Назад"
    }

    private void handleNotLoggedInState(long chatId, String messageText) {
        switch (messageText) {
            case "Регистрация":
                startRegistration(chatId);
                break;
            case "Вход":
                startLogin(chatId);
                break;

            default:
                sendNotLoggedInMenu(chatId);
        }
    }
    private void startLogin(long chatId) {
        userStates.put(chatId, UserState.LOGIN_EMAIL);
        sendMessage(chatId, "Введите ваш email:", createBackKeyboardMarkup());
    }
    private void handleLoginState(long chatId, String messageText, UserState state) {
        Map<String, String> loginData = registrationData.getOrDefault(chatId, new HashMap<>());
        switch (state) {
            case LOGIN_EMAIL:
                loginData.put("email", messageText);
                userStates.put(chatId, UserState.LOGIN_PASSWORD);
                sendMessage(chatId, "Введите ваш пароль:", createBackKeyboardMarkup());
                break;
            case LOGIN_PASSWORD:
                loginData.put("password", messageText);
                checkLogin(chatId, loginData.get("email"), loginData.get("password"));
                break;
        }
        registrationData.put(chatId, loginData);
    }
    private void checkLogin(long chatId, String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password_hash = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println(email.equals("admin") && password.equals("admin"));
                if (email.equals("admin") && password.equals("admin")) { // Проверяем на админа
                    sendMessage(chatId, "Вы успешно вошли в систему.");
                    userStates.put(chatId, UserState.ADMIN_PANEL);
                    showAdminPanel(chatId); // Переводим сразу в админ-панель
                } else {
                    sendMessage(chatId, "Вы успешно вошли в систему.");
                    userStates.put(chatId, UserState.LOGGED_IN);
                    sendLoggedInMenu(chatId);
                }
            } else {
                sendMessage(chatId, "Неверный email или пароль. Попробуйте снова.");
                userStates.put(chatId, UserState.NOT_LOGGED_IN);
                sendNotLoggedInMenu(chatId);
            }
        } catch (SQLException e) {
            sendMessage(chatId, "Ошибка при входе в систему: " + e.getMessage());
            System.out.println("DEBUG: SQL Exception in checkLogin: " + e.getMessage());
            userStates.put(chatId, UserState.NOT_LOGGED_IN);
            sendNotLoggedInMenu(chatId);
        }
    }
    private void sendNotLoggedInMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Добро пожаловать! \uD83C\uDF89\n Вы оказались в самом доступном кинотеатре \uD83E\uDD29\n У нас ВСЕ билеты по самой низкой цене — всего 200 рублей! \uD83D\uDCB8\n \n \uD83D\uDD25 Наслаждайтесь лучшими фильмами по невероятно доступной цене! \uD83C\uDFAC\n \n \uFE0F Вы не вошли в систему. Пожалуйста, выберите одно из действий: \uD83D\uDC47\n");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Регистрация");
        row.add("Вход");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }



    private void sendLoggedInMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Вы вошли в систему! Выберите действие:");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Купить билет");
        row1.add("Оставить отзыв");
        row1.add("Пополнить баланс");
        row1.add("Мои билеты");
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();

        // Добавляем новую кнопку
        row2.add("Рейтинг фильмов");
        row3.add("Выйти");
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private int getUserIdByChatId(long chatId) {
        String sql = "SELECT user_id FROM users WHERE chat_id = ?";
        int userId = -1;
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, chatId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                userId = resultSet.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userId;
    }

    private void viewTickets(long chatId) {
        int userId = getUserIdByChatId(chatId);
        if (userId == -1) {
            sendMessage(chatId, "Ошибка: не удалось найти пользователя.");
            userStates.put(chatId, UserState.LOGGED_IN);
            return;
        }

        List<TicketInfo> tickets = getUserTickets(userId);
        if (tickets.isEmpty()) {
            sendMessage(chatId, "У вас нет купленных билетов.");
            userStates.put(chatId, UserState.LOGGED_IN);
            sendLoggedInMenu(chatId);
            return;
        }

        StringBuilder messageText = new StringBuilder("Ваши билеты:\n\n");
        for (TicketInfo ticket : tickets) {
            messageText.append(ticket.toString()).append("\n");
        }

        sendMessage(chatId, messageText.toString());
        userStates.put(chatId, UserState.LOGGED_IN);
        sendLoggedInMenu(chatId);
    }

    private List<TicketInfo> getUserTickets(int userId) {
        List<TicketInfo> tickets = new ArrayList<>();
        String sql = "SELECT t.ticket_id, m.title, st.showtime, st.available_seats " +
                "FROM tickets t " +
                "JOIN showtimes st ON t.showtime_id = st.showtime_id " +
                "JOIN movies m ON st.movie_id = m.movie_id " +
                "WHERE t.user_name = (SELECT first_name FROM users WHERE user_id = ?) " +
                "ORDER BY st.showtime";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int ticketId = resultSet.getInt("ticket_id");
                String movieTitle = resultSet.getString("title");
                Timestamp showtime = resultSet.getTimestamp("showtime");
                int availableSeats = resultSet.getInt("available_seats");
                TicketInfo ticketInfo = new TicketInfo(ticketId, movieTitle, showtime, availableSeats);
                tickets.add(ticketInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tickets;
    }



    private void handleTopUpBalanceAmount(long chatId, String amountText) {
        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                sendMessage(chatId, "Сумма пополнения должна быть положительной.");
                userStates.put(chatId, UserState.TOP_UP_BALANCE);
                return;
            }
            int userId = getUserIdByChatId(chatId);
            if (userId == -1) {
                sendMessage(chatId, "Ошибка: не удалось найти пользователя.");
                userStates.put(chatId, UserState.LOGGED_IN);
                return;
            }
            if (updateBalance(userId, amount)) {
                sendMessage(chatId, "Баланс успешно пополнен на " + amount + "!");
            } else {
                sendMessage(chatId, "Ошибка при пополнении баланса.");
            }
            userStates.put(chatId, UserState.LOGGED_IN);
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Неверный формат суммы. Пожалуйста, введите сумму в виде десятичного числа.");
            userStates.put(chatId, UserState.TOP_UP_BALANCE);
        }
    }
    private void buyTicket(long chatId) {
        List<ShowtimeInfo> showtimes = getShowtimesList();
        if (showtimes.isEmpty()) {
            sendMessage(chatId, "В настоящее время нет доступных сеансов.");
            return;
        }
        sendMessage(chatId, "Выберите сеанс:", createShowtimesKeyboard(showtimes));
        userStates.put(chatId, UserState.BUYING_TICKET);
    }
    private boolean buyTicketForShowtime(int showtimeId, int userId, long chatId) {
        String updateSeatsSql = "UPDATE showtimes SET available_seats = available_seats - 1 " +
                "WHERE showtime_id = ? AND available_seats > 0";

        String insertTicketSql = "INSERT INTO tickets (user_name, chat_id, showtime_id) " +
                "VALUES ((SELECT first_name FROM users WHERE user_id = ?), ?, ?)";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            connection.setAutoCommit(false);

            try {
                // 1. Обновляем количество мест
                System.out.println("Attempting to update available seats for showtime ID: " + showtimeId);
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateSeatsSql)) {
                    preparedStatement.setInt(1, showtimeId);
                    int affectedRows = preparedStatement.executeUpdate();

                    if (affectedRows > 0) {
                        System.out.println("Seats updated successfully. Affected rows: " + affectedRows);

                        // 2. Списываем деньги
                        System.out.println("Attempting to update balance for user ID: " + userId);
                        if (updateBalance(connection, userId, -TICKET_PRICE)) {
                            System.out.println("Balance updated successfully.");

                            // 3. Создаем запись о транзакции
                            System.out.println("Attempting to save transaction for user ID: " + userId);
                            saveTransaction(connection, userId, showtimeId, -TICKET_PRICE, "payment");
                            System.out.println("Transaction saved successfully.");

                            // 4. Создаем билет
                            System.out.println("Attempting to insert ticket for user ID: " + userId + " and showtime ID: " + showtimeId);
                            try (PreparedStatement ticketStatement = connection.prepareStatement(insertTicketSql)) {
                                ticketStatement.setInt(1, userId);
                                ticketStatement.setLong(2, chatId);
                                ticketStatement.setInt(3, showtimeId);
                                ticketStatement.executeUpdate();
                            }

                            connection.commit();
                            System.out.println("Ticket purchase completed successfully.");
                            return true;
                        } else {
                            System.out.println("Failed to update balance for user ID: " + userId);
                        }
                    } else {
                        System.out.println("No available seats for showtime ID: " + showtimeId);
                    }
                }

                connection.rollback();
                System.out.println("Transaction rolled back due to failure.");
                return false;

            } catch (SQLException e) {
                connection.rollback();
                System.out.println("SQLException occurred: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    // Обновляем метод handleBuyTicket, чтобы передавать chatId
    private void handleBuyTicket(long chatId, String messageText) {
        if (messageText.equals("Отмена")) {
            sendMessage(chatId, "Покупка билета отменена.");
            userStates.put(chatId, UserState.LOGGED_IN);
            sendLoggedInMenu(chatId);
            return;
        }

        int showtimeId = extractShowtimeId(messageText);
        if (showtimeId == -1) {
            sendMessage(chatId, "Неверный формат выбора сеанса. Попробуйте еще раз.");
            return;
        }

        int userId = getUserIdByChatId(chatId);
        if (userId == -1) {
            sendMessage(chatId, "Ошибка: не удалось найти пользователя.");
            userStates.put(chatId, UserState.LOGGED_IN);
            return;
        }

        // Передаем chatId в метод buyTicketForShowtime
        if (buyTicketForShowtime(showtimeId, userId, chatId)) {
            sendMessage(chatId, "Билет успешно куплен!");
        } else {
            sendMessage(chatId, "К сожалению, не удалось купить билет. Возможно, закончились места или недостаточно средств.");
        }

        userStates.put(chatId, UserState.LOGGED_IN);
        sendLoggedInMenu(chatId);
    }




    private void leaveReview(long chatId) {
        int userId = getUserIdByChatId(chatId);
        if (userId == -1) {
            sendMessage(chatId, "Ошибка: не удалось найти пользователя.");
            return;
        }

        // 1. Получаем список доступных фильмов
        List<String> movies = getMoviesList();
        if (movies.isEmpty()) {
            sendMessage(chatId, "В базе данных нет фильмов. Добавьте фильмы, прежде чем оставлять отзыв.");
            return;
        }

        // 2. Предлагаем выбрать фильм
        sendMessage(chatId, "Выберите фильм, о котором хотите оставить отзыв:", createKeyboardMarkup(movies, true)); // Добавьте "Назад"
        userStates.put(chatId, UserState.SELECTING_MOVIE_FOR_REVIEW);
    }


    private void handleSelectMovieForReview(long chatId, String messageText) {
        int movieId = getMovieIdByTitle(messageText);
        if (movieId == -1) {
            sendMessage(chatId, "Ошибка: не удалось найти фильм.");
            userStates.put(chatId, UserState.LOGGED_IN);
            return;
        }

        // 3. Запрашиваем оценку
        sendMessage(chatId, "Оцените фильм от 1 до 5:", createBackKeyboardMarkup());
        userStates.put(chatId, UserState.RATING_FOR_REVIEW);
        selectedMovie.put(chatId, messageText); // Сохраняем выбранный фильм
    }

// ... (rest of the code)

    private void handleRatingForReview(long chatId, String ratingText) {
        int userId = getUserIdByChatId(chatId);
        if (userId == -1) {
            sendMessage(chatId, "Ошибка: не удалось найти пользователя.");
            userStates.put(chatId, UserState.LOGGED_IN);
            return;
        }

        int rating;
        try {
            rating = Integer.parseInt(ratingText);
            if (rating < 1 || rating > 5) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Пожалуйста, введите целое число от 1 до 5.");
            userStates.put(chatId, UserState.RATING_FOR_REVIEW);
            return;
        }

        // 4. Запрашиваем комментарий
        sendMessage(chatId, "Введите ваш комментарий:", createBackKeyboardMarkup());
        userStates.put(chatId, UserState.COMMENT_FOR_REVIEW);
        selectedRating.put(chatId, rating); // Сохраняем выбранную оценку
    }

// ... (rest of the code)

    private void handleCommentForReview(long chatId, String comment) {
        int userId = getUserIdByChatId(chatId);
        if (userId == -1) {
            sendMessage(chatId, "Ошибка: не удалось найти пользователя.");
            userStates.put(chatId, UserState.LOGGED_IN);
            return;
        }

        String movieTitle = selectedMovie.get(chatId);
        int movieId = getMovieIdByTitle(movieTitle);
        if (movieId == -1) {
            sendMessage(chatId, "Ошибка: не удалось найти фильм.");
            userStates.put(chatId, UserState.LOGGED_IN);
            return;
        }

        int rating = selectedRating.get(chatId);

        // 5. Сохраняем отзыв в базу данных
        boolean success = saveReviewToDatabase(userId, movieId, rating, comment);
        if (success) {
            sendMessage(chatId, "Отзыв успешно добавлен!");
            userStates.put(chatId, UserState.LOGGED_IN);
            sendLoggedInMenu(chatId);
        } else {
            sendMessage(chatId, "Ошибка при добавлении отзыва.");
            userStates.put(chatId, UserState.LOGGED_IN);
            sendLoggedInMenu(chatId);
        }
        selectedMovie.remove(chatId);
        selectedRating.remove(chatId);
    }

    private boolean saveReviewToDatabase(int userId, int movieId, int rating, String comment) {
        String sql = "INSERT INTO reviews (user_id, movie_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, movieId);
            preparedStatement.setInt(3, rating);
            preparedStatement.setString(4, comment);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Версия для прямого вызова (пополнение баланса)
    private boolean updateBalance(int userId, double amount) {
        // Открытие соединения с базой данных
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            connection.setAutoCommit(false); // Начало транзакции

            try {
                // Попытка обновления баланса
                boolean success = updateBalance(connection, userId, amount);

                if (success) {
                    System.out.println("Balance updated successfully for user ID: " + userId);

                    // Добавляем запись в таблицу transactions
                    saveTransaction(connection, userId, null, amount, amount > 0 ? "deposit" : "withdrawal");

                    connection.commit(); // Фиксация транзакции
                    return true;
                } else {
                    System.out.println("Failed to update balance for user ID: " + userId + ". Rolling back.");
                    connection.rollback(); // Откат транзакции в случае ошибки
                    return false;
                }
            } catch (SQLException e) {
                System.out.println("Error during balance update or transaction save: " + e.getMessage());
                connection.rollback(); // Откат транзакции при возникновении исключения
                e.printStackTrace();
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    // Версия для использования внутри других транзакций
    private boolean updateBalance(Connection connection, int userId, double amount) throws SQLException {
        String sql = "UPDATE users SET balance = balance + ? WHERE user_id = ? AND (balance + ?) >= 0";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setDouble(1, amount);
            preparedStatement.setInt(2, userId);
            preparedStatement.setDouble(3, amount); // Проверка на достаточность средств

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Balance updated successfully for user ID: " + userId + ", amount: " + amount);
                return true;
            } else {
                System.out.println("Failed to update balance. Insufficient funds for user ID: " + userId + ", attempted amount: " + amount);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("SQL error in updateBalance for user ID: " + userId + ": " + e.getMessage());
            throw e; // Проброс ошибки наверх для корректного отката транзакции
        }
    }

    private void saveTransaction(Connection connection, int userId, Integer showtimeId,
                                 double amount, String transactionType) throws SQLException {
        String sql = "INSERT INTO transactions (user_id, showtime_id, amount, transaction_type) " +
                "VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            if (showtimeId != null) {
                preparedStatement.setInt(2, showtimeId);
            } else {
                preparedStatement.setNull(2, Types.INTEGER);
            }
            preparedStatement.setDouble(3, amount);
            preparedStatement.setString(4, transactionType);
            preparedStatement.executeUpdate();
        }
    }
    private void sendMessage(long chatId, String text) {
        sendMessage(chatId, text, null);
    }
    private void sendMessage(long chatId, String text, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        if (keyboardMarkup != null) {
            message.setReplyMarkup(keyboardMarkup);
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private List<String> getMoviesList() {
        List<String> movies = new ArrayList<>();
        String sql = "SELECT title FROM movies";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                movies.add(resultSet.getString("title"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }
    private boolean isValidTime(String time) {
        String regex = "^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$";
        return time.matches(regex);
    }
}