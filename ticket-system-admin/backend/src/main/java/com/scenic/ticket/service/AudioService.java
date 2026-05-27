package com.scenic.ticket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 验票音效服务
 * 核销成功播放"滴"一声，失败播放"嘟嘟"两声
 */
@Slf4j
@Service
public class AudioService {

    private static final String SOUNDS_DIR = "assets/sounds/";
    private static final String SUCCESS_SOUND = SOUNDS_DIR + "success.wav";
    private static final String FAIL_SOUND = SOUNDS_DIR + "fail.wav";

    /**
     * 播放成功音效
     */
    public void playSuccess() {
        playSound(SUCCESS_SOUND);
    }

    /**
     * 播放失败音效
     */
    public void playFail() {
        playSound(FAIL_SOUND);
    }

    private void playSound(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            log.warn("音效文件不存在: {}", filePath);
            return;
        }

        try {
            File soundFile = path.toFile();
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
            clip.drain();
            clip.close();
            audioIn.close();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            log.error("播放音效失败: {}", filePath, e);
        }
    }
}