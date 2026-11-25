package org.telegram.utils.streak;

import java.util.Set;

/**
 * Rules for the NOVA chat streak. Pure Java, no Android dependencies, so it can be tested on a JVM.
 *
 * A day counts when both people wrote in the chat that day. Missing a day, or a day where only one
 * person wrote, breaks the streak. The streak is shown from SHOW_FROM_DAYS days in a row.
 * A broken streak can be restored for one missed day, using a restore from the balance.
 * The balance gets MONTHLY_GRANT restore each month and is capped at MAX_RESTORES.
 *
 * Days are local epoch days. Both clients compute the same result from the same chat history.
 */
public final class NovaStreak {
    public static final int SHOW_FROM_DAYS = 3;
    public static final int MAX_RESTORES = 3;
    public static final int MONTHLY_GRANT = 1;

    /** Maps an epoch day to a month index (any increasing number per calendar month). */
    public interface MonthOf {
        int month(long epochDay);
    }

    public static final class Result {
        /** Current streak length in days, 0 if none. */
        public final int days;
        /** True when the streak is long enough to show (days >= SHOW_FROM_DAYS). */
        public final boolean visible;
        /** True when today is not mutual yet: the streak is kept until the day ends. */
        public final boolean pendingToday;
        /** Length of the streak that was broken most recently, 0 if none. */
        public final int brokenLength;
        /** Epoch day when the most recent streak was broken, -1 if none. */
        public final long brokenDay;
        /** Restores left in the balance. */
        public final int restoresLeft;

        Result(int days, boolean visible, boolean pendingToday, int brokenLength, long brokenDay, int restoresLeft) {
            this.days = days;
            this.visible = visible;
            this.pendingToday = pendingToday;
            this.brokenLength = brokenLength;
            this.brokenDay = brokenDay;
            this.restoresLeft = restoresLeft;
        }
    }

    private NovaStreak() {}

    private static long minDay(Set<Long> days) {
        long min = Long.MAX_VALUE;
        for (Long d : days) {
            if (d != null && d < min) {
                min = d;
            }
        }
        return min;
    }

    /**
     * @param myDays       epoch days when I wrote in this chat
     * @param otherDays    epoch days when the other person wrote in this chat
     * @param restoredDays epoch days restored with a restore (they count as mutual)
     * @param todayDay     today as an epoch day
     */
    public static Result compute(Set<Long> myDays, Set<Long> otherDays, Set<Long> restoredDays,
                                 long todayDay, MonthOf monthOf) {
        long start = Long.MAX_VALUE;
        start = Math.min(start, minDay(myDays));
        start = Math.min(start, minDay(otherDays));
        start = Math.min(start, minDay(restoredDays));
        if (start == Long.MAX_VALUE || start > todayDay) {
            return new Result(0, false, false, 0, -1, 0);
        }

        int streak = 0;
        int brokenLength = 0;
        long brokenDay = -1;
        int balance = 0;
        int lastMonth = Integer.MIN_VALUE;
        boolean pendingToday = false;

        for (long d = start; d <= todayDay; d++) {
            int month = monthOf.month(d);
            if (month != lastMonth) {
                balance = Math.min(MAX_RESTORES, balance + MONTHLY_GRANT);
                lastMonth = month;
            }

            boolean mutual = myDays.contains(d) && otherDays.contains(d);
            if (!mutual && restoredDays.contains(d) && balance > 0 && streak > 0) {
                balance--;
                mutual = true;
            }

            if (mutual) {
                streak++;
            } else if (d == todayDay) {
                // today is not over yet: keep the streak, it is still pending
                pendingToday = true;
            } else {
                if (streak > 0) {
                    brokenLength = streak;
                    brokenDay = d;
                }
                streak = 0;
            }
        }

        return new Result(streak, streak >= SHOW_FROM_DAYS, pendingToday, brokenLength, brokenDay, balance);
    }
}
