package com.gabriel.aranias.go4lunch_v2;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gabriel.aranias.go4lunch_v2.model.Message;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;

public class MessageUnitTest {

    private static final String colId = "colId";
    private static final String docId = "docId";
    private static final String senderId = "senderId";
    private static final String receiverId = "receiverId";
    private static final String content = "content";
    private static final String date = "date";
    private FirebaseFirestore db;
    private Message message;

    @Before
    public void setUp() {
        db = mock(FirebaseFirestore.class, RETURNS_DEEP_STUBS);
        CollectionReference collectionReference = mock(CollectionReference.class, RETURNS_DEEP_STUBS);
        DocumentReference documentReference = mock(DocumentReference.class);

        when(db.collection(anyString())).thenReturn(collectionReference);
        when(collectionReference.document(anyString())).thenReturn(documentReference);

        message = mock(Message.class, RETURNS_DEEP_STUBS);
    }

    @Test
    public void createMessageInFirestore() {
        db.collection(colId).document(docId).set(message);
        verify(db.collection(colId).document(docId), times(1)).set(message);
    }

    @Test
    public void deleteMessageFromFirestore() {
        createMessageInFirestore();
        db.collection(colId).document(docId).delete();
        verify(db.collection(colId).document(docId), times(1)).delete();
    }

    @Test
    public void getMessageDetails() {
        when(message.getSenderId()).thenReturn(senderId);
        assertEquals(senderId, message.getSenderId());

        when(message.getReceiverId()).thenReturn(receiverId);
        assertEquals(receiverId, message.getReceiverId());

        when(message.getContent()).thenReturn(content);
        assertEquals(content, message.getContent());

        when(message.getDate()).thenReturn(date);
        assertEquals(date, message.getDate());
    }
}
