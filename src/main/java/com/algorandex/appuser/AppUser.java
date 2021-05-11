package com.algorandex.appuser;

import java.util.Collection;
import java.util.Collections;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.algorandex.model.HoldemWinType;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class AppUser implements UserDetails {

	@SequenceGenerator(
			name = "app_user_sequence",
			sequenceName = "app_user_sequence",
			allocationSize = 1
	)
	@Id
	@GeneratedValue(
			strategy = GenerationType.SEQUENCE,
			generator = "app_user_sequence"
	)
	private Long id;
	private String firstName;
	private String lastName;
	private String username;
	private String email;
	private String password;
	private Double balance;
	private Double amountBetThisRound;
	private HoldemWinType holdemWinType;
	private String holdemWinString;
	private Boolean folded;
	private String[] currentHand;
	@Enumerated(EnumType.STRING)
	private AppUserRole appUserRole;
	private Boolean locked = false;
	private Boolean enabled = false;
	
    public AppUser( String firstName,
					String lastName,
					String username,
					String email,
					String password,
					AppUserRole appUserRole ) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.username = username;
		this.email = email;
		this.password = password;
		this.appUserRole = appUserRole;
		this.balance = 1000.0;
		this.currentHand = null;
		this.amountBetThisRound = 0.0;
		this.folded = false;
		this.holdemWinType = null;
		this.holdemWinString = null;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority(appUserRole.name());
		return Collections.singletonList(authority);
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return !locked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public String getEmail() {
		return email;
	}
	
	public Double getBalance() {
		return this.balance;
	}
	
	public String[] getCurrentHand() {
		return this.currentHand;
	}
	
	public HoldemWinType getHoldemWinType() {
		return this.holdemWinType;
	}
	
	public String getHoldemWinString() {
		return this.holdemWinString;
	}
	
	public void setCurrentHand(String[] cards) {
		this.currentHand = cards;
	}

	public void addToAmountBetThisRound(Double amount) {
		this.amountBetThisRound += amount;
	}
	
	public void addToBalance(Double amount) {
		this.balance += amount;
	}
	
	public void subtractFromBalance(Double amount) {
		this.balance -= amount;
	}
	
	public Boolean setHoldemWinType(HoldemWinType holdemWinType) { 
		if (this.holdemWinType != null) {
			// Replace appUser's winType if the winType being passed is of higher rank. 
			if (holdemWinType == null) {
				this.holdemWinType = holdemWinType;
				return true;
			} else if (holdemWinType.getValue() < this.holdemWinType.getValue()) {
				this.holdemWinType = holdemWinType;
				return true;
			} else {
				return false;
			}
		} else {
			this.holdemWinType = holdemWinType;
			return true;
		}
	}
	
	public void setHoldemWinString(String holdemWinString) {
		this.holdemWinString = holdemWinString;
	}
}
