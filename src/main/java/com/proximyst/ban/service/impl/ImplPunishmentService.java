//
// ban - A punishment suite for Velocity.
// Copyright (C) 2020 Mariell Hoversholm
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.
//

package com.proximyst.ban.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.proximyst.ban.inject.annotation.VelocityExecutor;
import com.proximyst.ban.model.Punishment;
import com.proximyst.ban.service.IDataService;
import com.proximyst.ban.service.IPunishmentService;
import com.proximyst.ban.utils.ThrowableUtils;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Singleton
public final class ImplPunishmentService implements IPunishmentService {
  private final @NonNull IDataService dataService;
  private final @NonNull Executor executor;
  private final @NonNull Cache<@NonNull UUID, @NonNull List<@NonNull Punishment>> punishmentCache =
      CacheBuilder.newBuilder()
          .initialCapacity(512)
          .maximumSize(1024)
          .expireAfterAccess(5, TimeUnit.MINUTES)
          .build();

  @Inject
  public ImplPunishmentService(
      final @NonNull IDataService dataService,
      final @NonNull @VelocityExecutor Executor executor) {
    this.dataService = dataService;
    this.executor = executor;
  }

  @Override
  public @NonNull CompletableFuture<@NonNull ImmutableList<@NonNull Punishment>> getPunishments(
      final @NonNull UUID target) {
    final List<Punishment> existingPunishments = this.punishmentCache.getIfPresent(target);
    if (existingPunishments != null) {
      return CompletableFuture.completedFuture(ImmutableList.copyOf(existingPunishments));
    }

    // Okay, we've got to load them and cache them thereafter.
    return ThrowableUtils.supplyAsyncSneaky(
        () -> ImmutableList.copyOf(
            this.punishmentCache.get(target, () -> this.dataService.getPunishmentsForTarget(target))),
        this.executor);
  }

  @Override
  public @NonNull CompletableFuture<@Nullable Void> savePunishment(final @NonNull Punishment punishment) {
    return CompletableFuture.supplyAsync(() -> {
      this.dataService.savePunishment(punishment);
      return null;
    }, this.executor);
  }
}
