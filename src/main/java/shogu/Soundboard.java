package shogu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;

import javazoom.jl.player.Player;

public class Soundboard extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(Soundboard.class.getName());
    private static final String SOUND_FOLDER = "sounds";
    private static final int MAX_BUTTONS = 24;
    private JButton[] buttons;
    private File[] soundFiles;
    private BackgroundPanel backgroundPanel;
    private JSlider volumeSlider;
    private float currentVolume = 0.7f;
    private SourceDataLine currentSourceLine;
    private static final int BUFFER_SIZE = 16384;
    private JCheckBox localPlaybackCheckbox;
    private boolean enableLocalPlayback = true;

    public Soundboard() {
        setTitle("Soundboard");
        setSize(600, 400);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        initializeUI();
        setVisible(true);
    }

    private void initializeUI() {
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(600, 450));
        setContentPane(layeredPane);

        backgroundPanel = new BackgroundPanel("background.jpg");
        backgroundPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        backgroundPanel.setBounds(0, 0, 600, 450);
        layeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);

        buttons = new JButton[MAX_BUTTONS];
        soundFiles = new File[MAX_BUTTONS];
        Color buttonBackgroundColor = new Color(100, 149, 237);
        Color buttonTextColor = Color.BLACK;

        for (int i = 0; i < MAX_BUTTONS; i++) {
            buttons[i] = new JButton("");
            buttons[i].setPreferredSize(new Dimension(100, 50));
            buttons[i].setBackground(buttonBackgroundColor);
            buttons[i].setForeground(buttonTextColor);
            int index = i;
            buttons[i].addActionListener((ActionEvent _) -> {
                if (soundFiles[index] != null) {
                    playSound(soundFiles[index]);
                }
            });
            backgroundPanel.add(buttons[i]);
        }

        JButton addSoundButton = new JButton("Add Sound");
        addSoundButton.setPreferredSize(new Dimension(100, 50));
        addSoundButton.setBackground(new Color(65, 105, 225));
        addSoundButton.setForeground(buttonTextColor);
        addSoundButton.addActionListener((ActionEvent _) -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Audio Files", "wav", "mp3"));
            int returnValue = fileChooser.showOpenDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                addSoundFile(filePath);
            }
        });
        addSoundButton.setBounds(450, 250, 100, 50);
        layeredPane.add(addSoundButton, JLayeredPane.PALETTE_LAYER);

        initializeVolumeControl();
        initializeLocalPlaybackControl();
    }

    private void initializeVolumeControl() {
        JPanel volumePanel = new JPanel();
        volumePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        volumePanel.setOpaque(false);
        volumePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        JLabel volumeLabel = new JLabel("Volume:");
        volumeLabel.setForeground(Color.WHITE);
        volumeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 70);
        volumeSlider.setUI(new ModernSliderUI(volumeSlider));
        volumeSlider.setPreferredSize(new Dimension(200, 40));
        volumeSlider.setOpaque(false);
        volumeSlider.setForeground(Color.WHITE);
        volumeSlider.setPaintTicks(false);
        volumeSlider.setPaintLabels(false);
        volumeSlider.setFocusable(false);
        
        JLabel volumeLowIcon = new JLabel("🔈");
        volumeLowIcon.setForeground(Color.WHITE);
        volumeLowIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        
        JLabel volumeHighIcon = new JLabel("🔊");
        volumeHighIcon.setForeground(Color.WHITE);
        volumeHighIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        
        volumeSlider.addChangeListener(_ -> {
            currentVolume = volumeSlider.getValue() / 100.0f;
            System.out.println("Volume changed to: " + currentVolume);
            updateCurrentVolume();
        });
        
        JPanel sliderWithIconsPanel = new JPanel();
        sliderWithIconsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        sliderWithIconsPanel.setOpaque(false);
        
        sliderWithIconsPanel.add(volumeLowIcon);
        sliderWithIconsPanel.add(volumeSlider);
        sliderWithIconsPanel.add(volumeHighIcon);
        
        volumePanel.add(volumeLabel);
        volumePanel.add(sliderWithIconsPanel);
        
        backgroundPanel.add(volumePanel);
    }
    
    private void initializeLocalPlaybackControl() {
        JPanel localPlaybackPanel = new JPanel();
        localPlaybackPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        localPlaybackPanel.setOpaque(false);
        localPlaybackPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        localPlaybackCheckbox = new JCheckBox("Enable Local Playback");
        localPlaybackCheckbox.setSelected(enableLocalPlayback);
        localPlaybackCheckbox.setForeground(Color.WHITE);
        localPlaybackCheckbox.setOpaque(false);
        
        localPlaybackCheckbox.addActionListener(_ -> {
            enableLocalPlayback = localPlaybackCheckbox.isSelected();
            System.out.println("Local playback " + (enableLocalPlayback ? "enabled" : "disabled"));
        });
        
        localPlaybackPanel.add(localPlaybackCheckbox);
        
        backgroundPanel.add(localPlaybackPanel);
    }

    private void updateCurrentVolume() {
        if (currentSourceLine != null && currentSourceLine.isOpen()) {
            try {
                if (currentSourceLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) currentSourceLine.getControl(FloatControl.Type.MASTER_GAIN);
                    float dB = (float) (Math.log10(Math.max(currentVolume, 0.0001f)) * 20.0f);
                    dB = Math.max(dB, gainControl.getMinimum());
                    dB = Math.min(dB, gainControl.getMaximum());
                    gainControl.setValue(dB);
                    System.out.println("Volume updated to: " + dB + " dB");
                } else if (currentSourceLine.isControlSupported(FloatControl.Type.VOLUME)) {
                    FloatControl volumeControl = (FloatControl) currentSourceLine.getControl(FloatControl.Type.VOLUME);
                    volumeControl.setValue(currentVolume);
                    System.out.println("Volume updated to: " + currentVolume);
                }
            } catch (Exception ex) {
                System.out.println("Error updating volume: " + ex.getMessage());
            }
        }
    }

    public void fileManager() {
        File folder = new File(SOUND_FOLDER);

        if (!folder.exists() || !folder.isDirectory()) {
            int response = JOptionPane.showConfirmDialog(null, 
                    "The file'sounds' is invalid. Would you like to create it ?", 
                    "Dossier manquant", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                boolean created = folder.mkdir();
                if (created) {
                    JOptionPane.showMessageDialog(null, " 'sounds' file successfully created.");
                } else {
                    JOptionPane.showMessageDialog(null, "Error in the creation of the file.");
                    System.exit(1);
                }
            } else {
                JOptionPane.showMessageDialog(null, "The application cant work whithout the 'sounds' file.");
                System.exit(1);
            }
        }
        loadSoundFiles();
    }

    private void loadSoundFiles() {
        File folder = new File(SOUND_FOLDER);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".wav") || name.endsWith(".mp3"));
        
        if (files == null || files.length == 0) {
            JOptionPane.showMessageDialog(null, "No song found in the file : 'sounds'.");
            return;
        }

        for (int i = 0; i < Math.min(files.length, MAX_BUTTONS); i++) {
            soundFiles[i] = files[i];
            buttons[i].setText(files[i].getName());
        }
    }

    public void addSoundFile(String filePath) {
        File newFile = new File(filePath);
        if (newFile.exists() && (newFile.getName().endsWith(".wav") || newFile.getName().endsWith(".mp3"))) {
            try {
                File dest = new File(SOUND_FOLDER, newFile.getName());
                if (newFile.renameTo(dest)) {
                    JOptionPane.showMessageDialog(null, "Le fichier " + newFile.getName() + " a été ajouté avec succès.");
                    loadSoundFiles();
                } else {
                    JOptionPane.showMessageDialog(null, "Erreur lors de l'ajout du fichier.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erreur lors de l'ajout du fichier : " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Le fichier n'est pas valide ou n'est pas au format '.wav' ou '.mp3'.");
        }
    }

    private void playSound(File soundFile) {
        if (currentSourceLine != null && currentSourceLine.isOpen()) {
            currentSourceLine.flush();
            currentSourceLine.close();
            currentSourceLine = null;
        }

        if (soundFile.getName().endsWith(".mp3")) {
            playMP3Sound(soundFile);
        } else {
            playWavSound(soundFile);
        }
    }
    
    private void playWavSound(File soundFile) {
        try {
            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
            Mixer selectedMixer = null;
            for (Mixer.Info info : mixerInfo) {
                if (info.getName().contains("CABLE Input") && !info.getName().contains("Port")) {
                    selectedMixer = AudioSystem.getMixer(info);
                    System.out.println("Mixer sélectionné : " + info.getName());
                    break;
                }
            }
            if (selectedMixer == null) {
                JOptionPane.showMessageDialog(this, "CABLE Input (VB-Audio) not found.");
                return;
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(soundFile), BUFFER_SIZE));
            AudioFormat baseFormat = audioInputStream.getFormat();
            System.out.println("Original format: " + baseFormat);
            AudioFormat targetFormat;
            if (baseFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED || baseFormat.getEncoding() == AudioFormat.Encoding.PCM_FLOAT) {
                targetFormat = baseFormat;
            } else {
                targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);
            }
            AudioInputStream convertedStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
            System.out.println("Playing with format: " + targetFormat);
            DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, targetFormat);
            currentSourceLine = (SourceDataLine) selectedMixer.getLine(lineInfo);
            currentSourceLine.open(targetFormat, BUFFER_SIZE * 4);
            currentSourceLine.start();
            if (currentSourceLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) currentSourceLine.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log10(Math.max(currentVolume, 0.0001f)) * 20.0f);
                dB = Math.max(dB, gainControl.getMinimum());
                dB = Math.min(dB, gainControl.getMaximum());
                gainControl.setValue(dB);
                System.out.println("Volume set to: " + dB + " dB");
            } else if (currentSourceLine.isControlSupported(FloatControl.Type.VOLUME)) {
                FloatControl volumeControl = (FloatControl) currentSourceLine.getControl(FloatControl.Type.VOLUME);
                volumeControl.setValue(currentVolume);
                System.out.println("Volume set to: " + currentVolume);
            }
            Thread playbackThread = new Thread(() -> {
                try {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    SourceDataLine localLine = null;
                    if (enableLocalPlayback) {
                        DataLine.Info localLineInfo = new DataLine.Info(SourceDataLine.class, targetFormat);
                        localLine = (SourceDataLine) AudioSystem.getLine(localLineInfo);
                        localLine.open(targetFormat, BUFFER_SIZE * 4);
                        localLine.start();
                        if (localLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                            FloatControl gainControl = (FloatControl) localLine.getControl(FloatControl.Type.MASTER_GAIN);
                            float dB = (float) (Math.log10(Math.max(currentVolume, 0.0001f)) * 20.0f);
                            dB = Math.max(dB, gainControl.getMinimum());
                            dB = Math.min(dB, gainControl.getMaximum());
                            gainControl.setValue(dB);
                        } else if (localLine.isControlSupported(FloatControl.Type.VOLUME)) {
                            FloatControl volumeControl = (FloatControl) localLine.getControl(FloatControl.Type.VOLUME);
                            volumeControl.setValue(currentVolume);
                        }
                    }
                    while ((bytesRead = convertedStream.read(buffer, 0, buffer.length)) != -1) {
                        if (bytesRead > 0) {
                            currentSourceLine.write(buffer, 0, bytesRead);
                            if (enableLocalPlayback && localLine != null) {
                                localLine.write(buffer, 0, bytesRead);
                            }
                        }
                    }
                    currentSourceLine.drain();
                    if (enableLocalPlayback && localLine != null) {
                        localLine.drain();
                        localLine.close();
                    }
                    convertedStream.close();
                    audioInputStream.close();
                    currentSourceLine.stop();
                    currentSourceLine.close();
                    System.out.println("Playback completed normally");
                } catch (IOException | LineUnavailableException e) {
                    LOGGER.log(Level.SEVERE, "Error : {0}", e.getMessage());
                }
            });
            playbackThread.setDaemon(true);
            playbackThread.start();
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException ex) {
            LOGGER.log(Level.SEVERE, "Error playing sound: {0}", ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error playing sound: " + ex.getMessage());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error occurred: {0}", ex.getMessage());
            JOptionPane.showMessageDialog(this, "Unexpected error: " + ex.getMessage());
        }
    }

    private void playMP3Sound(File soundFile) {
        try {
            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
            Mixer selectedMixer = Arrays.stream(mixerInfo)
                .filter(info -> info.getName().contains("CABLE Input") && !info.getName().contains("Port"))
                .findFirst()
                .map(AudioSystem::getMixer)
                .orElse(null);
    
            if (selectedMixer == null) {
                JOptionPane.showMessageDialog(this, "CABLE Input (VB-Audio) not found for MP3.");
                return;
            }
    
            Thread playbackThread = new Thread(() -> {
                try {
                    CustomAudioDevice audioDevice = new CustomAudioDevice(selectedMixer, currentVolume, enableLocalPlayback);
                    currentSourceLine = audioDevice.getSourceLine();
    
                    try (FileInputStream fis = new FileInputStream(soundFile);
                         BufferedInputStream bis = new BufferedInputStream(fis, BUFFER_SIZE * 2)) {
                        
                        System.out.println("Playing MP3: " + soundFile.getName());
                        
                        Player player = new Player(bis, audioDevice);
                        player.play();
                        
                        System.out.println("MP3 playback completed");
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Error playing sound: ", ex.getMessage());
                        JOptionPane.showMessageDialog(this, "Error playing sound with MP3 mixeur " + ex.getMessage());
                    } finally {
                        audioDevice.close();
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error playing sound: ", ex.getMessage());
                    JOptionPane.showMessageDialog(Soundboard.this, "Error playing MP3: " + ex.getMessage());
                }
            });
            
            playbackThread.setDaemon(true);
            playbackThread.start();
            
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error playing sound: ", ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error setting up MP3 playback: " + ex.getMessage());
        }
    }
}