package session

type SessionCreateRequest struct {
	Username string `json:"username,omitempty"`
	Password string `json:"password,omitempty"`
	SsoHost  string `json:"ssoHost,omitempty"`
	SsoMode  bool   `json:"ssoMode,omitempty"`
}

type SessionCreateResponse struct {
	Token        string
	RefreshToken string
	Err          error
}

func (r SessionCreateResponse) Error() error { return r.Err }
