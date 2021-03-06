/*
 * This file is part of FlexibleLogin
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2018 contributors
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

import com.github.games647.flexiblelogin.AttemptManager;
import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.ProtectionManager;
import com.github.games647.flexiblelogin.storage.Account;

import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

public class LoginTask implements Runnable {

    private final FlexibleLogin plugin;
    private final AttemptManager attemptManager;
    private final ProtectionManager protectionManager;

    private final Player player;
    private final String userInput;

    public LoginTask(FlexibleLogin plugin, AttemptManager attemptManager, ProtectionManager protectionManager, Player player, String userInput) {
        this.plugin = plugin;
        this.attemptManager = attemptManager;
        this.protectionManager = protectionManager;
        this.player = player;
        this.userInput = userInput;
    }

    @Override
    public void run() {
        Optional<Account> optAccount = plugin.getDatabase().loadAccount(player);
        if (!optAccount.isPresent()) {
            player.sendMessage(plugin.getConfigManager().getText().getAccountNotFound());
            return;
        }

        try {
            Account account = optAccount.get();
            if (account.checkPassword(plugin.getHasher(), userInput)) {
                attemptManager.clearAttempts(player.getUniqueId());

                //update the ip
                account.setIP(player.getConnection().getAddress().getAddress());
                account.setLoggedIn(true);

                player.sendMessage(plugin.getConfigManager().getText().getLoggedIn());
                Task.builder().execute(() -> protectionManager.unprotect(player)).submit(plugin);

                //flushes the ip update
                plugin.getDatabase().save(account);
            } else {
                player.sendMessage(plugin.getConfigManager().getText().getIncorrectPassword());
            }
        } catch (Exception ex) {
            plugin.getLogger().error("Unexpected error while password checking", ex);
            player.sendMessage(plugin.getConfigManager().getText().getErrorExecutingCommand());
        }
    }
}
