package backend.academy.bot.telegram.command.session;

@FunctionalInterface
public interface SessionStateHandler {
    /**
     * Обрабатывает событие
     * @param state текущие состояние сессии
     * @param event событие
     * @return true если сессия остается активной, иначе false
     */
    boolean handle(SessionState state, SessionEvent event);
}
