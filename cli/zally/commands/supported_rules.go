package commands

import (
	"bytes"
	"fmt"
	"io/ioutil"

	"encoding/json"

	"strings"

	"github.com/urfave/cli"
	"github.com/zalando-incubator/zally/cli/zally/domain"
	"github.com/zalando-incubator/zally/cli/zally/utils"
)

// SupportedRulesCommand lists supported rules
var SupportedRulesCommand = cli.Command{
	Name:   "rules",
	Usage:  "List supported rules",
	Action: listRules,
	Flags: []cli.Flag{
		cli.StringFlag{
			Name:  "type",
			Usage: "Rules Type",
		},
	},
}

var supportedTypes = []string{}

func listRules(c *cli.Context) error {
	requestBuilder := utils.NewRequestBuilder(c.GlobalString("linter-service"), c.GlobalString("token"), c.App)
	ruleType := strings.ToLower(c.String("type"))
	err := validateType(ruleType)
	if err != nil {
		return err
	}

	rules, err := fetchRules(requestBuilder, ruleType)
	if err != nil {
		return err
	}

	printRules(rules)

	return nil
}

func validateType(ruleType string) error {
	switch ruleType {
	case
		"must",
		"should",
		"may",
		"hint",
		"":
		return nil
	}
	return fmt.Errorf("%s is not supported", ruleType)
}

func fetchRules(requestBuilder *utils.RequestBuilder, rulesType string) (*domain.Rules, error) {
	uri := "/supported-rules?is_active=true"
	if rulesType != "" {
		uri += "&type=" + rulesType
	}
	request, err := requestBuilder.Build("GET", uri, nil)
	if err != nil {
		return nil, err
	}

	response, err := utils.DoHTTPRequest(request)
	if err != nil {
		return nil, err
	}

	if response.StatusCode != 200 {
		defer response.Body.Close()
		body, _ := ioutil.ReadAll(response.Body)

		return nil, fmt.Errorf(
			"Cannot submit file for linting. HTTP Status: %d, Response: %s", response.StatusCode, string(body))
	}

	decoder := json.NewDecoder(response.Body)

	var rules domain.Rules
	decoder.Decode(&rules)

	return &rules, nil
}

func printRules(rules *domain.Rules) {
	var buffer bytes.Buffer
	resultPrinter := utils.NewResultPrinter(&buffer)
	resultPrinter.PrintRules(rules)

	fmt.Print(buffer.String())
}
