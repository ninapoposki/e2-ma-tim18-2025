package com.example.habitforge.data.repository;

import com.example.habitforge.application.model.AllianceMission;
import com.example.habitforge.data.firebase.UserRemoteDataSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AllianceMissionRepository {

    private final UserRemoteDataSource remoteDb;

    public AllianceMissionRepository() {
        this.remoteDb = new UserRemoteDataSource();
    }

    // kreiranje nove misije
    public void createAllianceMission(AllianceMission mission, GenericCallback callback) {
        FirebaseFirestore db = remoteDb.getFirestore();

        db.collection("allianceMissions")
                .add(mission)
                .addOnSuccessListener(docRef -> {
                    mission.setId(docRef.getId());
                    docRef.update("id", docRef.getId());
                    callback.onComplete(true);
                })
                .addOnFailureListener(e -> callback.onComplete(false));
    }


    // azuriranje napretka clana
    public void updateMemberProgress(String missionId, String userId, int addedXP, GenericCallback callback) {
        FirebaseFirestore db = remoteDb.getFirestore();

        db.collection("allianceMissions").document(missionId)
                .update("progress." + userId, FieldValue.increment(addedXP))
                .addOnSuccessListener(aVoid -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    // dohvatanje misije u savezu
    public void getMissionsForAlliance(String allianceId, AllianceMissionListCallback callback) {
        FirebaseFirestore db = remoteDb.getFirestore();

        db.collection("allianceMissions")
                .whereEqualTo("allianceId", allianceId)
                .get()
                .addOnSuccessListener(query -> {
                    List<AllianceMission> missions = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        AllianceMission mission = doc.toObject(AllianceMission.class);
                        missions.add(mission);
                    }
                    callback.onSuccess(missions);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // zavrsavanje misije
    public void completeAllianceMission(String missionId, GenericCallback callback) {
        FirebaseFirestore db = remoteDb.getFirestore();

        db.collection("allianceMissions").document(missionId)
                .update("completed", true, "active", false, "endTime", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    public interface GenericCallback {
        void onComplete(boolean success);
    }
    public void getAllianceMemberCount(String allianceId, MemberCountCallback onSuccess, FailureCallback onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("alliances").document(allianceId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        List<String> members = (List<String>) document.get("memberIds");
                        int count = (members != null) ? members.size() : 1;
                        onSuccess.onResult(count);
                    } else {
                        onSuccess.onResult(1);
                    }
                })
                .addOnFailureListener(onFailure::onFailure);
    }
    public void getMissionById(String missionId, SingleMissionCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("allianceMissions")
                .document(missionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        AllianceMission mission = documentSnapshot.toObject(AllianceMission.class);
                        if (mission != null) {
                            mission.setId(documentSnapshot.getId());
                            callback.onSuccess(mission);
                        } else {
                            callback.onFailure(new Exception("Mission object is null"));
                        }
                    } else {
                        callback.onFailure(new Exception("Mission not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }


    //za dodeljivnaje direktno misije

    public void getAllianceMembers(String allianceId, MembersListCallback onSuccess, FailureCallback onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("alliances").document(allianceId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        List<String> members = (List<String>) document.get("memberIds");
                        onSuccess.onResult(members != null ? members : new ArrayList<>());
                    } else {
                        onSuccess.onResult(new ArrayList<>());
                    }
                })
                .addOnFailureListener(onFailure::onFailure);
    }

    public void assignMissionToMembers(List<String> memberIds, AllianceMission mission, GenericCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (memberIds == null || memberIds.isEmpty()) {
            callback.onComplete(false);
            return;
        }

        // 🔹 Dodaj misiju u "users/{id}/missions" za svakog člana
        for (String memberId : memberIds) {
            db.collection("users").document(memberId)
                    .collection("missions")
                    .document(mission.getId())
                    .set(mission);
        }

        callback.onComplete(true);
    }

    public void updateBossHp(String missionId, int newHp, GenericCallback callback) {
        FirebaseFirestore db = remoteDb.getFirestore();
        db.collection("allianceMissions").document(missionId)
                .update("bossHP", newHp)
                .addOnSuccessListener(aVoid -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }


    public interface MembersListCallback {
        void onResult(List<String> memberIds);
    }


    public interface MemberCountCallback {
        void onResult(int count);
    }

    public interface FailureCallback {
        void onFailure(Exception e);
    }


    public interface AllianceMissionListCallback {
        void onSuccess(List<AllianceMission> missions);
        void onFailure(Exception e);
    }
    public interface SingleMissionCallback {
        void onSuccess(AllianceMission mission);
        void onFailure(Exception e);
    }

}
