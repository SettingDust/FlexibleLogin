/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 games647 and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.games647.flexiblelogin.tasks;

import com.github.games647.flexiblelogin.Account;
import com.github.games647.flexiblelogin.FlexibleLogin;

import java.util.Optional;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

public abstract class ResetPwTask implements Runnable {

    protected final FlexibleLogin plugin = FlexibleLogin.getInstance();

    protected final CommandSource src;
    protected final String password;

    public ResetPwTask(CommandSource src, String password) {
        this.src = src;
        this.password = password;
    }

    @Override
    public void run() {
        Optional<Player> player = getIfPresent();
        if (player.isPresent()) {
            Account account = plugin.getDatabase().getAccountIfPresent(player.get());
            resetPassword(Optional.ofNullable(account));
        } else {
            resetPassword(loadAccount());
        }
    }

    private void resetPassword(Optional<Account> account) {
        if (account.isPresent()) {
            try {
                account.get().setPasswordHash(plugin.getHasher().hash(password));
                src.sendMessage(plugin.getConfigManager().getText().getChangePassword());
            } catch (Exception ex) {
                plugin.getLogger().error("Error creating hash", ex);
                src.sendMessage(plugin.getConfigManager().getText().getErrorCommand());
            }
        } else {
            src.sendMessage(plugin.getConfigManager().getText().getAccountNotFound());
        }
    }

    public abstract Optional<Player> getIfPresent();

    public abstract Optional<Account> loadAccount();
}
