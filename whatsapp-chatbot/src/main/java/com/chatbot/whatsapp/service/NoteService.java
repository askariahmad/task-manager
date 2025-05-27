package com.chatbot.whatsapp.service;

import com.chatbot.whatsapp.model.Note;
import com.chatbot.whatsapp.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NoteService {

    private final NoteRepository noteRepository;

    @Autowired
    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public String addNote(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "Note content cannot be empty.";
        }
        Note newNote = new Note(content.trim());
        noteRepository.save(newNote);
        String preview = newNote.getContent().substring(0, Math.min(newNote.getContent().length(), 30)) + (newNote.getContent().length() > 30 ? "..." : "");
        return "Note added (ID: " + newNote.getId() + "): '" + preview + "'";
    }

    public String listNotes() {
        List<Note> notes = noteRepository.findAll(); // Or add sorting if desired, e.g., Sort.by(Sort.Direction.DESC, "createdAt")
        if (notes.isEmpty()) {
            return "You have no notes.";
        }
        return "Your Notes:\n" +
                notes.stream()
                        .map(note -> "(ID: " + note.getId() + ") " + note.getContent())
                        .collect(Collectors.joining("\n"));
    }

    public String findNote(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "Please provide a keyword to search for in notes.";
        }
        List<Note> foundNotes = noteRepository.findByContentContainingIgnoreCase(keyword.trim());
        if (foundNotes.isEmpty()) {
            return "No notes found containing the keyword: '" + keyword + "'.";
        }
        return "Notes found containing '" + keyword + "':\n" +
                foundNotes.stream()
                        .map(note -> "(ID: " + note.getId() + ") " + note.getContent())
                        .collect(Collectors.joining("\n"));
    }

    public String deleteNote(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return "Please provide an ID for the note to delete.";
        }
        Optional<Note> noteOpt = noteRepository.findById(identifier);
        if (noteOpt.isPresent()) {
            noteRepository.deleteById(identifier);
            return "Note (ID: " + identifier + ") removed.";
        }
        // Optional: try deleting by keyword if it was a unique match, but ID is safer.
        // For simplicity, we'll stick to ID-based deletion for now.
        return "Note not found with ID: '" + identifier + "'.";
    }
}
