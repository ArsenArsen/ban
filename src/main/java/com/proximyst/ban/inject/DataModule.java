package com.proximyst.ban.inject;

import com.google.inject.AbstractModule;
import com.proximyst.ban.BanPlugin;
import com.proximyst.ban.data.IDataInterface;
import com.proximyst.ban.data.IMojangApi;
import com.proximyst.ban.manager.MessageManager;
import com.proximyst.ban.manager.PunishmentManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jdbi.v3.core.Jdbi;

public final class DataModule extends AbstractModule {
  @NonNull
  private final BanPlugin main;

  public DataModule(@NonNull BanPlugin main) {
    this.main = main;
  }

  @Override
  protected void configure() {
    bind(IDataInterface.class).toProvider(main::getDataInterface);
    bind(PunishmentManager.class).toProvider(main::getPunishmentManager);
    bind(MessageManager.class).toProvider(main::getMessageManager);
    bind(IMojangApi.class).toProvider(main::getMojangApi);
    bind(Jdbi.class).toProvider(main::getJdbi);
  }
}
