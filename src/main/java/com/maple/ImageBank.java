package com.maple;

import java.awt.image.BufferedImage;

public class ImageBank {
  public BufferedImage inventoryIcon;
  public BufferedImage teleportRockIcon;
  public BufferedImage teleportRockSelect;
  public BufferedImage teleportRockOk;
  public BufferedImage teleportRockCancel;
  public BufferedImage cashShopIcon;
  public BufferedImage inCashShop;
  public BufferedImage suicideSpot;
  public BufferedImage mesoSpot;
  public BufferedImage revive;
  public BufferedImage dead;
  public BufferedImage blackheart;
  public BufferedImage haku;
  public BufferedImage mapleIcon;
  public BufferedImage portalLocation;
  public BufferedImage onFantasyThemePark;
  public BufferedImage facingLeft;
  public BufferedImage facingRight;
  public BufferedImage fullHealth;
  public BufferedImage notFullHealth;
  public String path;

  public ImageBank() {
    path = Util.pathToResources + "indicators/";
    inventoryIcon = Util.read("item.png");
    teleportRockIcon = Util.read("hypertele.png");
    teleportRockSelect = Util.read("move.png");
    teleportRockOk = Util.read("hyperteleok.png");
    teleportRockCancel = Util.read("hypertelecancel.png");
    cashShopIcon = Util.read("cashshop.png");
    inCashShop = Util.read("incashshop.png");
    suicideSpot = Util.read("suicidespot.png");
    mesoSpot = Util.read("mesofarmspot.png");
    revive = Util.read("deathok.png");
    dead = Util.read("dead.png");
    blackheart = Util.read("blackheart.png");
    haku = Util.read("haku.png");
    mapleIcon = Util.read("mapleIcon.png");
    portalLocation = Util.read("portalLocation.png");
    onFantasyThemePark = Util.read("fantasythemepark.png");
    facingLeft = Util.read("facingLeft.png");
    facingRight = Util.read("facingRight.png");
    fullHealth = Util.read("fullhealth.png");
    notFullHealth = Util.read("notfullhealth.png");
  }
}
