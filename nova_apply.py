#!/usr/bin/env python3
"""Applies NOVA string/manifest/navigation patches on top of upstream Telegram sources.

Idempotent: re-running on an already patched tree is a no-op.
NovaSettingsActivity.java and nova_launcher.xml are committed as regular sources.
"""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent


def patch(path, old, new, label):
    p = ROOT / path
    if not p.exists():
        raise SystemExit(f"[NOVA] Missing file: {path}")
    data = p.read_text(encoding="utf-8")
    if new in data:
        print(f"[NOVA] Already patched: {path} ({label})")
        return
    count = data.count(old)
    if count != 1:
        raise SystemExit(f"[NOVA] Expected one match for {label}, found {count} in {path}")
    p.write_text(data.replace(old, new, 1), encoding="utf-8")
    print(f"[NOVA] Patched: {path} ({label})")


def require(path):
    if not (ROOT / path).exists():
        raise SystemExit(f"[NOVA] Required source missing: {path}")


require("TMessagesProj/src/main/java/org/telegram/ui/NovaSettingsActivity.java")
require("TMessagesProj/src/main/res/drawable/nova_launcher.xml")

EN = "TMessagesProj/src/main/res/values/strings.xml"
RU = "TMessagesProj/src/main/res/values-ru/strings.xml"
SETTINGS = "TMessagesProj/src/main/java/org/telegram/ui/SettingsActivity.java"
MANIFEST = "TMessagesProj/config/debug/AndroidManifest.xml"

patch(EN, '<string name="AppName">Telegram</string>', '<string name="AppName">NOVA</string>', "English app name")
patch(EN, '<string name="AppNameBeta">Telegram Beta</string>', '<string name="AppNameBeta">NOVA</string>', "English beta app name")
patch(EN, '    <string name="LanguageCode">en</string>', """    <string name="LanguageCode">en</string>
    <string name="NovaSection">NOVA</string>
    <string name="NovaSettings">NOVA settings</string>
    <string name="NovaSettingsInfo">Preview experimental NOVA options</string>
    <string name="NovaPreview">Preview options</string>
    <string name="NovaDemoAnimations">Enable NOVA animations (demo)</string>
    <string name="NovaDemoExperimental">Show experimental options (demo)</string>
    <string name="NovaDemoHint">These switches are visual test controls. They do not change Telegram behavior yet.</string>
    <string name="NovaAbout">About NOVA</string>
    <string name="NovaAboutText">NOVA is an independent Telegram-based client. This is an early test build.</string>""", "English NOVA strings")
patch(RU, '<string name="AppName">Telegram</string>', '<string name="AppName">NOVA</string>', "Russian app name")
patch(RU, '<string name="AppNameBeta">Telegram Beta</string>', '<string name="AppNameBeta">NOVA</string>', "Russian beta app name")
patch(RU, '    <string name="LanguageCode">ru</string>', """    <string name="LanguageCode">ru</string>
    <string name="NovaSection">NOVA</string>
    <string name="NovaSettings">Настройки NOVA</string>
    <string name="NovaSettingsInfo">Предварительный просмотр экспериментальных настроек NOVA</string>
    <string name="NovaPreview">Параметры демо</string>
    <string name="NovaDemoAnimations">Анимации NOVA (демо)</string>
    <string name="NovaDemoExperimental">Показывать экспериментальные функции (демо)</string>
    <string name="NovaDemoHint">Эти переключатели нужны для проверки интерфейса. Пока они не меняют работу Telegram.</string>
    <string name="NovaAbout">О NOVA</string>
    <string name="NovaAboutText">NOVA — независимый клиент на базе Telegram. Это ранняя тестовая сборка.</string>""", "Russian NOVA strings")
patch(SETTINGS, '        items.add(UItem.asHeader(getString(R.string.SettingsHelp)));', """        items.add(UItem.asShadow(null));
        items.add(UItem.asHeader(getString(R.string.NovaSection)));
        items.add(SettingCell.Factory.of(28, 0xFF8B5CF6, 0xFF4169E1, R.drawable.settings_language, getString(R.string.NovaSettings), getString(R.string.NovaSettingsInfo)));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.SettingsHelp)));""", "NOVA settings row")
patch(SETTINGS, """            case 27: {
                presentFragment(new HeaderSettingsActivity());
                break;
            }""", """            case 27: {
                presentFragment(new HeaderSettingsActivity());
                break;
            }
            case 28: {
                presentSettingFragment(new NovaSettingsActivity());
                break;
            }""", "NOVA settings navigation")
# The debug build uses config/debug/AndroidManifest.xml (see TMessagesProj_App/build.gradle).
patch(MANIFEST, 'android:icon="@mipmap/ic_launcher"', 'android:icon="@drawable/nova_launcher"', "NOVA launcher icon")
patch(MANIFEST, 'android:roundIcon="@mipmap/ic_launcher_round"', 'android:roundIcon="@drawable/nova_launcher"', "NOVA round launcher icon")

print("[NOVA] Done.")
