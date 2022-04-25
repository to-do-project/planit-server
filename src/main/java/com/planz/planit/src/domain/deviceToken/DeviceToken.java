package com.planz.planit.src.domain.deviceToken;

import com.planz.planit.src.domain.friend.FriendStatus;
import com.planz.planit.src.domain.user.User;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.apache.tomcat.jni.Local;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.planz.planit.src.domain.deviceToken.DeviceTokenFlag.ALL;

@Entity
@NoArgsConstructor
public class DeviceToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="device_token_id")
    private Long deviceTokenId;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @Column(name="device_token")
    private String deviceToken;

    @Column(name="friendFlag")
    private int friendFlag;
    @Column(name="group_flag")
    private int groupFlag;
    @Column(name="notice_flag")
    private int noticeFlag;
    @Column(name="setting_flag")
    private int settingFlag;

    @Column(name="all_flag")
    private int allFlag;

    @Column(name="create_at")
    private LocalDateTime createAt;

    @Column(name="update_at")
    private LocalDateTime updateAt;

    @Builder
    public DeviceToken(User user,String deviceToken){
        this.user = user;
        //초기 설정은 알림 설정 모두 켜져 있는걸로
        this.friendFlag=1;
        this.groupFlag=1;
        this.noticeFlag=1;
        this.settingFlag=1;
        this.createAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
    }

    //updateAt 갱신 메소드
    public void changeUpdateAt(){
        this.updateAt = LocalDateTime.now();
    }

    public void changeFlag(DeviceTokenFlag flag){
        switch (flag){
            case ALL:
                this.allFlag = this.allFlag==1?0:1;
                break;
            case FRIEND:
                this.friendFlag = this.friendFlag==1?0:1;
                break;
            case GROUP:
                this.groupFlag = this.groupFlag==1?0:1;
                break;
            case SETTING:
                this.settingFlag = this.settingFlag==1?0:1;
                break;
            case NOTICE:
                this.noticeFlag = this.noticeFlag==1?0:1;
                break;
        }
    }
}
