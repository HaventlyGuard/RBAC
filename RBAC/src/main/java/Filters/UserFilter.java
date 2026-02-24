package Filters;
import Models.User;
import java.util.function.Predicate;

@FunctionalInterface
public interface UserFilter extends Predicate<User> {

    boolean test(User user);

    default UserFilter and(UserFilter other) {
        return user -> this.test(user) && other.test(user);
    }

    default UserFilter or(UserFilter other) {
        return user -> this.test(user) || other.test(user);
    }

    default UserFilter negate() {
        return user -> !this.test(user);
    }
}
