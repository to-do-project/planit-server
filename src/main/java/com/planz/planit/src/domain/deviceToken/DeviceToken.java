package com.planz.planit.src.domain.deviceToken;

import com.planz.planit.src.domain.friend.FriendStatus;
import com.planz.planit.src.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.tomcat.jni.Local;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.planz.planit.src.domain.deviceToken.DeviceTokenFlag.ALL;
import static java.time.LocalDateTime.now;

@Getter
@Entity
@NoArgsConstructor
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name="devicetoken_uk",
                        columnNames = {"user_id", "device_token"}
                )
        }
)
public class DeviceToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="device_token_id")
    private Long deviceTokenId;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @Column(name="device_token", nullable = false)
    private String deviceToken;

    @Column(name="friend_flag")
    private int friendFlag = 1;
    @Column(name="group_flag")
    private int groupFlag = 1;
    @Column(name="notice_flag")
    private int noticeFlag = 1;
    @Column(name="setting_flag")
    private int settingFlag = 1;

    @Column(name="all_flag")
    private int allFlag = 1;

    @Column(name="create_at",nullable = false)
    private LocalDateTime createAt= now();

    @Column(name="update_at",nullable=false)
    private LocalDateTime updateAt=now();

    @Builder
    public DeviceToken(User user,String deviceToken){
        this.user = user;
        this.deviceToken = deviceToken;
    }

    @Override
    public String toString() {
        return "\ndeviceTokenId: "+this.getDeviceTokenId()
                +"\n userId: "+this.user.getUserId()
                +"\n deviceToken: "+this.deviceToken;
    }

    //updateAt 갱신 메소드
    public void changeUpdateAt(){
        this.updateAt = now();
    }

    public void changeFlag(DeviceTokenFlag flag){
        switch (flag){
            case ALL:
                System.out.println();
                this.allFlag = this.allFlag==1?0:1;
                this.friendFlag = this.allFlag;
                this.settingFlag = this.allFlag;
                this.groupFlag = this.allFlag;
                this.noticeFlag = this.allFlag;
                break;
            case FRIEND:
                this.friendFlag = this.friendFlag==1?0:1;
                this.allFlag = 0;
                break;
            case GROUP:
                this.groupFlag = this.groupFlag==1?0:1;
                this.allFlag = 0;
                break;
            case SETTING:
                this.settingFlag = this.settingFlag==1?0:1;
                this.allFlag = 0;
                break;
            case NOTICE:
                this.noticeFlag = this.noticeFlag==1?0:1;
                this.allFlag = 0;
                break;
        }

    }
}
